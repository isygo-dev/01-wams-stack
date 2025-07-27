package eu.isygoit.com.rest.service;

import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.exception.EmptyFileException;
import eu.isygoit.exception.EmptyFileListException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.helper.CRC16Helper;
import eu.isygoit.helper.CRC32Helper;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ILinkedFile;
import eu.isygoit.model.IMultiFileEntity;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract service class for handling multiple file operations for entities.
 * Supports uploading, downloading, and deleting additional files associated with entities implementing
 * {@link IMultiFileEntity}, {@link IIdAssignable}, and {@link ICodeAssignable}.
 *
 * @param <I>  the type of the identifier, extending {@link Serializable}
 * @param <T>  the entity type, extending {@link IMultiFileEntity}, {@link IIdAssignable}, and {@link ICodeAssignable}
 * @param <L>  the linked file type, extending {@link ILinkedFile}, {@link ICodeAssignable}, and {@link IIdAssignable}
 * @param <R>  the repository type for the entity, extending {@link JpaPagingAndSortingCodeAssingnableRepository}
 * @param <RL> the repository type for linked files, extending {@link JpaPagingAndSortingRepository}
 */
@Slf4j
public abstract class MultiFileService<
        I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I> & ICodeAssignable,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I>,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>,
        RL extends JpaPagingAndSortingRepository<L, I>
        > extends MultiFileServiceSubMethods<I, T, L, R, RL> implements IMultiFileServiceMethods<I, T> {

    private final Class<T> persistentClass;
    private final Class<L> linkedFileClass;

    /**
     * Constructor that initializes the persistent and linked file classes using reflection.
     */
    @SuppressWarnings("unchecked")
    protected MultiFileService() {
        var parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.persistentClass = (Class<T>) parameterizedType.getActualTypeArguments()[1];
        this.linkedFileClass = (Class<L>) parameterizedType.getActualTypeArguments()[2];
        log.debug("Initialized MultiFileService with persistentClass: {}, linkedFileClass: {}",
                persistentClass.getSimpleName(), linkedFileClass.getSimpleName());
    }

    /**
     * Uploads multiple additional files for the specified parent entity.
     *
     * @param parentId the ID of the parent entity
     * @param files    the array of multipart files to upload
     * @return the list of linked files associated with the parent entity
     * @throws IOException            if an I/O error occurs during file upload
     * @throws EmptyFileListException if the files array is null or empty
     */
    @Override
    public List<L> uploadAdditionalFiles(I parentId, MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            log.error("Empty or null file list provided for parentId: {}", parentId);
            throw new EmptyFileListException("Empty file list for parent ID: " + parentId);
        }

        var entity = getEntityOrThrow(parentId);
        log.debug("Uploading {} files for parentId: {}", files.length, parentId);
        for (var file : files) {
            uploadAdditionalFile(parentId, file);
        }
        log.info("Successfully uploaded {} files for parentId: {}", files.length, parentId);
        return entity.getAdditionalFiles();
    }

    /**
     * Uploads a single additional file for the specified parent entity.
     *
     * @param parentId the ID of the parent entity
     * @param file     the multipart file to upload
     * @return the list of linked files associated with the parent entity
     * @throws IOException        if an I/O error occurs during file upload
     * @throws EmptyFileException if the file is null or empty
     */
    @Override
    public List<L> uploadAdditionalFile(I parentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Null or empty file provided for parentId: {}", parentId);
            throw new EmptyFileException("Empty file for parent ID: " + parentId);
        }

        var entity = getEntityOrThrow(parentId);
        log.debug("Uploading file for parentId: {}", parentId);

        try {
            var linkedFile = createLinkedFile(file);

            linkedFile = beforeUpload(linkedFile, file);
            linkedFile = subUploadFile(file, linkedFile);
            linkedFile = afterUpload(linkedFile, file);

            if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
                entity.setAdditionalFiles(new ArrayList<>());
                log.debug("Initialized empty additional files list for entity: {}", parentId);
            }
            entity.getAdditionalFiles().add(linkedFile);
            update(entity);

            log.info("Successfully uploaded file for parentId: {}, fileName: {}", parentId, linkedFile.getFileName());
            return entity.getAdditionalFiles();
        } catch (Exception e) {
            log.error("Failed to upload file for parentId: {}", parentId, e);
            throw new IOException("Failed to upload additional file for parent ID: " + parentId, e);
        }
    }

    private L createLinkedFile(MultipartFile file) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        var linkedFile = linkedFileClass.getDeclaredConstructor().newInstance();
        assignCodeIfEmpty(linkedFile);

        var originalFilename = file.getOriginalFilename();
        linkedFile.setOriginalFileName(originalFilename);
        linkedFile.setExtension(FilenameUtils.getExtension(originalFilename));
        linkedFile.setPath(Path.of(getUploadDirectory())
                .resolve(persistentClass.getSimpleName().toLowerCase())
                .resolve("additional")
                .toString());
        linkedFile.setMimetype(file.getContentType());
        var bytes = file.getBytes();
        linkedFile.setCrc16(CRC16Helper.calculate(bytes));
        linkedFile.setCrc32(CRC32Helper.calculate(bytes));
        linkedFile.setSize(file.getSize());
        linkedFile.setVersion(1L);
        return linkedFile;
    }

    /**
     * Downloads a file associated with the specified parent and file IDs.
     *
     * @param parentId the ID of the parent entity
     * @param fileId   the ID of the linked file
     * @param version  the version of the file to download
     * @return the resource DTO containing the file data
     * @throws IOException             if an I/O error occurs during file download
     * @throws ObjectNotFoundException if the parent entity or linked file is not found
     */
    @Override
    public ResourceDto downloadFile(I parentId, I fileId, Long version) throws IOException {
        var entity = getEntityOrThrow(parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            log.error("Linked file not found for fileId: {}, parentId: {}", fileId, parentId);
            throw new ObjectNotFoundException(linkedFileClass.getSimpleName() + " with ID: " + fileId);
        }
        log.debug("Downloading file for parentId: {}, fileId: {}, version: {}", parentId, fileId, version);
        return subDownloadFile(linkedFile, version);
    }

    /**
     * Deletes an additional file associated with the specified parent and file IDs.
     *
     * @param parentId the ID of the parent entity
     * @param fileId   the ID of the linked file to delete
     * @return true if the file was deleted successfully
     * @throws IOException           if an I/O error occurs during file deletion
     * @throws FileNotFoundException if the linked file is not found
     */
    @Override
    public boolean deleteAdditionalFile(I parentId, I fileId) throws IOException {
        var entity = getEntityOrThrow(parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            log.error("Linked file not found for fileId: {}, parentId: {}", fileId, parentId);
            throw new FileNotFoundException(linkedFileClass.getSimpleName() + " with ID: " + fileId);
        }
        entity.getAdditionalFiles().removeIf(file -> file.getId().equals(fileId));
        subDeleteFile(linkedFile);
        update(entity);
        log.info("Successfully deleted file for parentId: {}, fileId: {}", parentId, fileId);
        return true;
    }

    /**
     * Retrieves the entity by ID or throws an exception if not found.
     *
     * @param id the ID of the entity
     * @return the entity
     * @throws ObjectNotFoundException if the entity is not found
     */
    protected T getEntityOrThrow(I id) {
        return findById(id).orElseThrow(() -> {
            log.error("Entity not found for ID: {}", id);
            return new ObjectNotFoundException(persistentClass.getSimpleName() + " with ID: " + id);
        });
    }

    /**
     * Finds a linked file by ID within the entity's additional files.
     *
     * @param entity the entity
     * @param fileId the ID of the linked file
     * @return the linked file, or null if not found
     */
    protected L findLinkedFile(T entity, I fileId) {
        if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
            log.debug("No additional files found for entity: {}", entity.getCode());
            return null;
        }
        return entity.getAdditionalFiles().stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Hook method called before file upload, allowing customization.
     *
     * @param entity the linked file entity
     * @param file   the multipart file
     * @return the modified linked file entity
     * @throws IOException if an I/O error occurs
     */
    protected L beforeUpload(L entity, MultipartFile file) throws IOException {
        log.debug("Executing beforeUpload for entity: {}", entity.getCode());
        return entity;
    }

    /**
     * Hook method called after file upload, allowing customization.
     *
     * @param entity the linked file entity
     * @param file   the multipart file
     * @return the modified linked file entity
     * @throws IOException if an I/O error occurs
     */
    protected L afterUpload(L entity, MultipartFile file) throws IOException {
        log.debug("Executing afterUpload for entity: {}", entity.getCode());
        return entity;
    }

    /**
     * Gets the base upload directory for storing files.
     *
     * @return the upload directory path
     */
    protected abstract String getUploadDirectory();
}