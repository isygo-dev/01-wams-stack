package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.*;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Abstract service for handling file-related entities.
 *
 * @param <I> ID type
 * @param <T> Entity type (file-related entity)
 * @param <R> Repository type
 */
@Slf4j
public abstract class FileService<I, T extends IFileEntity & IIdEntity & ICodifiable, R extends JpaPagingAndSortingRepository>
        extends FileServiceSubMethods<I, T, R>
        implements IFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    // Functional interface for executing before upload logic
    private final BiFunction<String, T, T> beforeUploadProcess = (domain, entity) -> {
        try {
            log.info("Executing beforeUploadProcess for entity: {}", entity.getClass().getSimpleName());
            return beforeUpload(domain, entity, null);
        } catch (IOException e) {
            log.error("Error during beforeUploadProcess: {}", e.getMessage(), e);
            throw new RuntimeException("Error during beforeUpload", e);
        }
    };

    // Functional interface for executing after upload logic
    private final BiFunction<String, T, T> afterUploadProcess = (domain, entity) -> {
        try {
            log.info("Executing afterUploadProcess for entity: {}", entity.getClass().getSimpleName());
            return afterUpload(domain, entity, null);
        } catch (IOException e) {
            log.error("Error during afterUploadProcess: {}", e.getMessage(), e);
            throw new RuntimeException("Error during afterUpload", e);
        }
    };

    @Transactional
    @Override
    public T createWithFile(String senderDomain, T entity, MultipartFile file) throws IOException {
        log.info("Creating entity with file for domain: {}", senderDomain);
        processEntityWithFile(senderDomain, entity, file, this::createAndFlush);
        return processFileUpload(entity, file);
    }

    @Transactional
    @Override
    public T updateWithFile(String senderDomain, I id, T entity, MultipartFile file) throws IOException {
        log.info("Updating entity with ID: {} in domain: {}", id, senderDomain);
        if (!repository().existsById(id)) {
            log.error("Entity not found: ID {}", id);
            throw new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id);
        }
        entity.setId(id);
        processEntityWithFile(senderDomain, entity, file, this::updateAndFlush);
        return processFileUpload(entity, file);
    }

    @Transactional
    @Override
    public T uploadFile(String senderDomain, I id, MultipartFile file) throws IOException {
        log.info("Uploading file for entity ID: {}", id);
        T entity = getById(id).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        if (Objects.nonNull(file) && !file.isEmpty()) {
            processFile(entity, file);
            entity = updateAndFlush(entity);
            return processFileUpload(entity, file);
        }

        log.warn("Upload file skipped: File is null or empty for entity ID: {}", id);
        return entity;
    }

    @Override
    public Resource download(I id, Long version) throws IOException {
        log.info("Downloading file for entity ID: {} with version: {}", id, version);
        return subDownloadFile(getById(id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id)), version);
    }

    // Utility to process entity with file and persist it
    private void processEntityWithFile(String senderDomain, T entity, MultipartFile file, java.util.function.Function<T, T> persistFunction) {
        log.debug("Processing entity before persisting...");
        applyIf(entity, e -> setDomainForSaaSEntity(senderDomain, e));
        applyIf(entity, this::setCodeForEntityIfNeeded);
        applyIf(entity, e -> {
            if (Objects.nonNull(file) && !file.isEmpty()) processFile(e, file);
        });
        persistFunction.apply(entity);
        log.debug("Entity processing complete.");
    }

    // Process file upload logic with functional processing
    private T processFileUpload(T entity, MultipartFile file) throws IOException {
        if (Objects.nonNull(file) && !file.isEmpty()) {
            String domain = getDomain(entity);
            log.info("Processing file upload for entity: {} in domain: {}", entity.getClass().getSimpleName(), domain);
            entity = beforeUploadProcess.apply(domain, entity);
            subUploadFile(file, entity);
            return afterUploadProcess.apply(domain, entity);
        }
        log.warn("No file to upload for entity: {}", entity.getClass().getSimpleName());
        return entity;
    }

    // Generic helper method to apply a function if the entity is not null
    private void applyIf(T entity, Consumer<T> action) {
        if (entity != null) action.accept(entity);
    }

    // Helper method to set domain for SAAS entity
    private void setDomainForSaaSEntity(String senderDomain, T entity) {
        if (entity instanceof ISAASEntity isaasEntity && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            isaasEntity.setDomain(senderDomain);
            log.debug("Set domain to {} for entity {}", senderDomain, entity.getClass().getSimpleName());
        }
    }

    // Helper method to set code if missing
    private void setCodeForEntityIfNeeded(T entity) {
        if (!StringUtils.hasText(entity.getCode())) {
            this.getNextCode().ifPresent(code -> {
                entity.setCode(code);
                log.debug("Assigned new code: {} to entity {}", code, entity.getClass().getSimpleName());
            });
        }
    }

    // Process the file (set path, filename, extension)
    private void processFile(T entity, MultipartFile file) {
        entity.setPath(getUploadDirectory() +
                File.separator + getDomain(entity) +
                File.separator + persistentClass.getSimpleName().toLowerCase());
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        log.info("Processed file: {} with extension: {} for entity: {}",
                file.getOriginalFilename(), entity.getExtension(), entity.getClass().getSimpleName());
    }

    // Helper method to get domain of an entity
    private String getDomain(T entity) {
        return (entity instanceof ISAASEntity isaasEntity) ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME;
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    public T beforeUpload(String domain, T entity, MultipartFile file) throws IOException {
        log.debug("Before upload for entity in domain: {}", domain);
        return entity;
    }

    public T afterUpload(String domain, T entity, MultipartFile file) throws IOException {
        log.debug("After upload for entity in domain: {}", domain);
        return entity;
    }

    public T beforeUpdate(T object) {
        log.debug("Before update for entity: {}", object.getClass().getSimpleName());
        return object;
    }

    public T afterUpdate(T object) {
        log.debug("After update for entity: {}", object.getClass().getSimpleName());
        return object;
    }

    public T beforeCreate(T object) {
        log.debug("Before create for entity: {}", object.getClass().getSimpleName());
        return object;
    }

    public T afterCreate(T object) {
        log.debug("After create for entity: {}", object.getClass().getSimpleName());
        return object;
    }
}
