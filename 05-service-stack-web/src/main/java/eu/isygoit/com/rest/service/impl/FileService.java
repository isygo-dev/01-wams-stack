package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * The type File service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileService<I, T extends IFileEntity & IIdEntity & ICodifiable, R extends JpaPagingAndSortingRepository>
        extends FileServiceSubMethods<I, T, R>
        implements IFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Transactional
    @Override
    public T createWithFile(String senderDomain, T entity, MultipartFile file) throws IOException {
        // Set domain for SAAS entity if applicable
        setDomainForSaaSEntity(senderDomain, entity);

        // Set code for entity if not already set
        setCodeForEntityIfNeeded(entity);

        // Process the file (path, original filename, extension)
        if (Objects.nonNull(file) && !file.isEmpty()) {
            processFile(entity, file);
        } else {
            log.warn("Create with file ({}) : File is null or empty", this.persistentClass.getSimpleName());
        }

        // Create the entity
        entity = this.createAndFlush(entity);

        // If file exists, upload and process it
        if (Objects.nonNull(file) && !file.isEmpty()) {
            entity = beforeUploadProcess(entity, file);
            subUploadFile(file, entity);
            return afterUploadProcess(entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(String senderDomain, I id, T entity, MultipartFile file) throws IOException {
        // Set domain for SAAS entity if applicable
        setDomainForSaaSEntity(senderDomain, entity);

        // Ensure entity exists and set the ID
        if (repository().existsById(id)) {
            entity.setId(id);

            // Process the file (path, original filename, extension)
            if (Objects.nonNull(file) && !file.isEmpty()) {
                processFile(entity, file);
            } else {
                log.warn("Update with file ({}) : File is null or empty", this.persistentClass.getSimpleName());
            }

            // Update the entity
            entity = this.updateAndFlush(entity);

            // If file exists, upload and process it
            if (Objects.nonNull(file) && !file.isEmpty()) {
                entity = beforeUploadProcess(entity, file);
                subUploadFile(file, entity);
                return afterUploadProcess(entity, file);
            }

            return entity;
        } else {
            throw new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id);
        }
    }

    @Transactional
    @Override
    public T uploadFile(String senderDomain, I id, MultipartFile file) throws IOException {
        T entity = findById(id).orElseThrow(() -> new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id));

        // Process the file (path, original filename, extension)
        if (Objects.nonNull(file) && !file.isEmpty()) {
            processFile(entity, file);
            entity = this.updateAndFlush(entity);

            // Upload file if it exists
            entity = beforeUploadProcess(entity, file);
            subUploadFile(file, entity);
            return afterUploadProcess(entity, file);
        } else {
            log.warn("Upload file ({}) : File is null or empty", this.persistentClass.getSimpleName());
        }

        return entity;
    }

    @Override
    public Resource downloadFile(I id, Long version) throws IOException {
        return subDownloadFile(findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id)), version);
    }

    // Helper method to set domain for SAAS entity
    private void setDomainForSaaSEntity(String senderDomain, T entity) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }
    }

    // Helper method to set code for the entity if not already set
    private void setCodeForEntityIfNeeded(T entity) {
        if (!StringUtils.hasText(entity.getCode())) {
            T finalEntity = entity;
            this.getNextCode().ifPresent(code -> finalEntity.setCode(code));
        }
    }

    // Helper method to process the file (set path, filename, extension)
    private void processFile(T entity, MultipartFile file) {
        entity.setPath(this.getUploadDirectory() +
                File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                File.separator + this.persistentClass.getSimpleName().toLowerCase());

        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
    }

    // Helper method for before upload process
    private T beforeUploadProcess(T entity, MultipartFile file) throws IOException {
        return beforeUpload((entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME), entity, file);
    }

    // Helper method for after upload process
    private T afterUploadProcess(T entity, MultipartFile file) throws IOException {
        return afterUpload((entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME), entity, file);
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    /**
     * Before upload t.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T beforeUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload t.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T afterUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    // Other methods for before and after updates, create
    public T beforeUpdate(T object) {
        return object;
    }

    public T afterUpdate(T object) {
        return object;
    }

    public T beforeCreate(T object) {
        return object;
    }

    public T afterCreate(T object) {
        return object;
    }
}
