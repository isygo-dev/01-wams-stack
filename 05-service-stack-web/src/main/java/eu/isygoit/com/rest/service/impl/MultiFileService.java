package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IMultiFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.encrypt.helper.CRC16;
import eu.isygoit.encrypt.helper.CRC32;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The type Multi file service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <L> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class MultiFileService<I, T extends IMultiFileEntity & IIdEntity, L extends ILinkedFile & ICodifiable & IIdEntity, R extends JpaPagingAndSortingRepository>
        extends MultiFileServiceSubMethods<I, T, L, R>
        implements IMultiFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    private final Class<L> linkedFileClass = (Class<L>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];

    @Override
    public List<L> uploadAdditionalFiles(I parentId, MultipartFile[] files) {
        T entity = findById(parentId).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + parentId));
        Arrays.stream(files).forEach(file -> {
            try {
                uploadAdditionalFile(parentId, file);
            } catch (IOException e) {
                throw new UploadFileException(e);
            }
        });
        return entity.getAdditionalFiles();
    }

    @Override
    public List<L> uploadAdditionalFile(I parentId, MultipartFile file) throws IOException {
        T entity = findById(parentId).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + parentId));
        if (Objects.nonNull(file) && !file.isEmpty()) {
            L linkedFile = createLinkedFile(entity, file);
            linkedFile = beforeUpload(entity, linkedFile, file);
            linkedFile = subUploadFile(file, linkedFile);
            afterUpload(entity, linkedFile, file);

            if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
                entity.setAdditionalFiles(new ArrayList<>());
            }
            entity.getAdditionalFiles().add(linkedFile);
        } else {
            log.warn("Upload file ({}) :File is null or empty", this.persistentClass.getSimpleName());
        }

        entity = this.update(entity);
        return entity.getAdditionalFiles();
    }

    @Override
    public Resource downloadFile(I parentId, I fileId, Long version) throws IOException {
        T entity = findById(parentId).orElseThrow(() -> new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + parentId));
        L linkedFile = findLinkedFileById(entity, fileId);
        if (linkedFile != null) {
            return subDownloadFile(linkedFile, version);
        } else {
            throw new ObjectNotFoundException(this.linkedFileClass.getSimpleName() + " with id " + fileId);
        }
    }

    @Override
    public boolean deleteAdditionalFile(I parentId, I fileId) throws IOException {
        T entity = findById(parentId).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + parentId));
        L linkedFile = findLinkedFileById(entity, fileId);
        if (linkedFile != null) {
            entity.getAdditionalFiles().removeIf(elm -> ((L) elm).getId().equals(fileId));
            subDeleteFile(linkedFile);
            this.update(entity);
            return true;
        } else {
            throw new FileNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
        }
    }

    // Helper method to create a linked file
    private L createLinkedFile(T entity, MultipartFile file) throws IOException {
        try {
            L linkedFile = linkedFileClass.newInstance();
            this.getNextCode().ifPresent(linkedFile::setCode);
            setDomainIfNeeded(entity, linkedFile);

            linkedFile.setOriginalFileName(file.getOriginalFilename());
            linkedFile.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            linkedFile.setPath(getFilePath(entity, file));
            linkedFile.setMimetype(file.getContentType());
            linkedFile.setCrc16(CRC16.calculate(file.getBytes()));
            linkedFile.setCrc32(CRC32.calculate(file.getBytes()));
            linkedFile.setSize(file.getSize());
            linkedFile.setVersion(1L);

            return linkedFile;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error creating linked file instance: ", e);
            throw new UploadFileException("Error creating linked file instance", e);
        }
    }

    // Helper method to set domain if needed
    private void setDomainIfNeeded(T entity, L linkedFile) {
        if (entity instanceof ISAASEntity isaasEntity && linkedFile instanceof ISAASEntity isaasLinkedFile) {
            isaasLinkedFile.setDomain(isaasEntity.getDomain());
        }
    }

    // Helper method to get the file path for a linked file
    private String getFilePath(T entity, MultipartFile file) {
        return this.getUploadDirectory() +
                File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                File.separator + persistentClass.getSimpleName().toLowerCase() + File.separator + "additional";
    }

    // Helper method to find a linked file by ID
    private L findLinkedFileById(T entity, I fileId) {
        return (L) entity.getAdditionalFiles().stream()
                .filter(item -> ((L) item).getId().equals(fileId)).findFirst()
                .orElse(null);
    }

    /**
     * Before upload l.
     *
     * @param entity     the entity
     * @param linkedFile the linked file
     * @param file       the file
     * @return the l
     * @throws IOException the io exception
     */
// Hook methods for pre- and post-processing
    public L beforeUpload(T entity, L linkedFile, MultipartFile file) throws IOException {
        return linkedFile;
    }

    /**
     * After upload l.
     *
     * @param entity     the entity
     * @param linkedFile the linked file
     * @param file       the file
     * @return the l
     * @throws IOException the io exception
     */
    public L afterUpload(T entity, L linkedFile, MultipartFile file) throws IOException {
        return linkedFile;
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();
}
