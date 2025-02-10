package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IMultiFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.encrypt.helper.CRC16;
import eu.isygoit.encrypt.helper.CRC32;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.UploadFileException;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class MultiFileService<I, T extends IMultiFileEntity & IIdEntity, L extends ILinkedFile & ICodifiable & IIdEntity, R extends JpaPagingAndSortingRepository>
        extends MultiFileServiceSubMethods<I, T, L, R>
        implements IMultiFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    private final Class<L> linkedFileClass = (Class<L>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];

    // Helper method to get the entity by ID
    private T getEntityById(I parentId) {
        return getById(parentId)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + parentId));
    }

    @Override
    public List<L> upload(I parentId, MultipartFile[] files) {
        T entity = getEntityById(parentId); // Fetch entity once

        // Log the upload process
        log.info("Uploading {} files for entity {} with ID {}", files.length, persistentClass.getSimpleName(), parentId);

        // Process each file and upload
        Arrays.stream(files).forEach(file -> {
            try {
                uploadSingleFile(parentId, file);
            } catch (IOException e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                throw new UploadFileException(e);
            }
        });

        return entity.getAdditionalFiles();
    }

    @Override
    public List<L> upload(I parentId, MultipartFile file) throws IOException {
        uploadSingleFile(parentId, file);  // Delegate to helper method for single file upload
        return getEntityById(parentId).getAdditionalFiles();  // Return updated files
    }

    // Helper method to handle a single file upload logic
    private void uploadSingleFile(I parentId, MultipartFile file) throws IOException {
        if (Objects.isNull(file) || file.isEmpty()) {
            log.warn("File is null or empty. Upload skipped.");
            return; // Early return if file is invalid
        }

        T entity = getEntityById(parentId); // Fetch the parent entity

        // Create the linked file and process it
        L linkedFile = createLinkedFile(entity, file);
        linkedFile = beforeUpload(entity, linkedFile, file); // Pre-upload hook
        linkedFile = subUploadFile(file, linkedFile); // Specific upload logic
        afterUpload(entity, linkedFile, file); // Post-upload hook

        // Initialize additionalFiles if empty
        if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
            entity.setAdditionalFiles(new ArrayList<>());
        }

        // Add the linked file to the entity
        entity.getAdditionalFiles().add(linkedFile);

        // Log successful upload
        log.info("File {} uploaded successfully for entity {} with ID {}", file.getOriginalFilename(), persistentClass.getSimpleName(), parentId);

        // Update entity in the database
        this.update(entity);
    }

    @Override
    public Resource download(I parentId, I fileId, Long version) throws IOException {
        T entity = getEntityById(parentId); // Fetch entity by ID

        // Find the linked file by ID
        L linkedFile = Optional.ofNullable(findLinkedFileById(entity, fileId))
                .orElseThrow(() -> new ObjectNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId));

        // Log download request
        log.info("Downloading file with ID {} for entity {} with ID {}", fileId, persistentClass.getSimpleName(), parentId);

        return subDownloadFile(linkedFile, version); // Return the resource for the file
    }

    @Override
    public boolean delete(I parentId, I fileId) throws IOException {
        T entity = getEntityById(parentId); // Fetch the entity

        // Find the linked file by its ID
        L linkedFile = Optional.ofNullable(findLinkedFileById(entity, fileId))
                .orElseThrow(() -> new FileNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId));

        // Remove the linked file from the additional files list
        entity.getAdditionalFiles().removeIf(elm -> ((L)elm).equals(fileId));
        subDeleteFile(linkedFile); // Handle specific file deletion logic

        // Update the entity
        this.update(entity);

        // Log successful deletion
        log.info("Deleted file with ID {} for entity {} with ID {}", fileId, persistentClass.getSimpleName(), parentId);

        return true;
    }

    private L createLinkedFile(T entity, MultipartFile file) throws IOException {
        try {
            L linkedFile = linkedFileClass.getDeclaredConstructor().newInstance();

            // Set additional file properties
            linkedFile.setOriginalFileName(file.getOriginalFilename());
            linkedFile.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            linkedFile.setPath(getFilePath(entity, file));
            linkedFile.setMimetype(file.getContentType());
            linkedFile.setCrc16(CRC16.calculate(file.getBytes()));
            linkedFile.setCrc32(CRC32.calculate(file.getBytes()));
            linkedFile.setSize(file.getSize());
            linkedFile.setVersion(1L);

            this.getNextCode().ifPresent(linkedFile::setCode); // Set next code
            setDomainIfNeeded(entity, linkedFile); // Set domain if needed

            return linkedFile;
        } catch (Exception e) {
            log.error("Error creating linked file instance: ", e);
            throw new UploadFileException("Error creating linked file instance", e);
        }
    }

    private void setDomainIfNeeded(T entity, L linkedFile) {
        if (entity instanceof ISAASEntity isaasEntity && linkedFile instanceof ISAASEntity isaasLinkedFile) {
            isaasLinkedFile.setDomain(isaasEntity.getDomain()); // Set domain if applicable
        }
    }

    private String getFilePath(T entity, MultipartFile file) {
        return this.getUploadDirectory() + File.separator +
                (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                File.separator + persistentClass.getSimpleName().toLowerCase() + File.separator + "additional";
    }

    private L findLinkedFileById(T entity, I fileId) {
        return (L) entity.getAdditionalFiles().stream()
                .filter(elm -> ((L)elm).equals(fileId))
                .findFirst()
                .orElse(null);
    }

    // Hook methods for pre- and post-processing
    public L beforeUpload(T entity, L linkedFile, MultipartFile file) throws IOException {
        return linkedFile;  // Can be overridden for additional pre-upload logic
    }

    public L afterUpload(T entity, L linkedFile, MultipartFile file) throws IOException {
        return linkedFile;  // Can be overridden for additional post-upload logic
    }

    protected abstract String getUploadDirectory();  // Abstract method to get the upload directory
}