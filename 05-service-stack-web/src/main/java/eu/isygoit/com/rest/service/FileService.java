package eu.isygoit.com.rest.service;

import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Abstract service providing file management logic for entities implementing IFileEntity.
 * Supports create, update, upload, and download operations with file processing.
 *
 * @param <I> ID type
 * @param <T> File entity type
 * @param <R> Repository type
 */
@Slf4j
public abstract class FileService<I extends Serializable, T extends IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingRepository<T, I>>
        extends FileServiceSubMethods<I, T, R>
        implements IFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    protected abstract String getUploadDirectory();

    // Optional hooks to override before and after upload/create/update
    public T beforeUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    public T afterUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    public T beforeCreate(T object) {
        return object;
    }

    public T afterCreate(T object) {
        return object;
    }

    public T beforeUpdate(T object) {
        return object;
    }

    public T afterUpdate(T object) {
        return object;
    }

    @Transactional
    @Override
    public T createWithFile(String senderDomain, T entity, MultipartFile file) throws IOException {
        setDomainIfApplicable(senderDomain, entity);

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(entity, file);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        entity = beforeCreate(entity);
        entity = createAndFlush(entity);
        entity = afterCreate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(String senderDomain, I id, T entity, MultipartFile file) throws IOException {
        setDomainIfApplicable(senderDomain, entity);

        T existing = repository().findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
        entity.setId(id);

        if (file != null && !file.isEmpty()) {
            assignOrPreserveCode(entity, existing);
            setFileAttributes(entity, file);
        } else {
            log.warn("UpdateWithFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        entity = beforeUpdate(entity);
        entity = updateAndFlush(entity);
        entity = afterUpdate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T uploadFile(String senderDomain, I id, MultipartFile file) throws IOException {
        T entity = findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(entity, file);
            entity = updateAndFlush(entity);

            return handleFileUpload(entity, file);
        } else {
            log.warn("UploadFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        return entity;
    }

    @Override
    public Resource downloadFile(I id, Long version) throws IOException {
        return findById(id)
                .map(entity -> subDownloadFile(entity, version))
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
    }

    /**
     * Utility: Assigns code if not already set.
     */
    private void assignCodeIfEmpty(T entity) {
        if (!StringUtils.hasText(entity.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        }
    }

    /**
     * Utility: Assigns code from existing or generates new.
     */
    private void assignOrPreserveCode(T entity, T existing) {
        if (!StringUtils.hasText(entity.getCode()) && !StringUtils.hasText(existing.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        } else {
            entity.setCode(existing.getCode());
        }
    }

    /**
     * Utility: Sets file attributes on the entity.
     */
    private void setFileAttributes(T entity, MultipartFile file) {
        Path path = Path.of(getUploadDirectory())
                .resolve(getEntityDomainOrDefault(entity))
                .resolve(persistentClass.getSimpleName().toLowerCase());

        entity.setPath(path.toString());
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
    }

    /**
     * Utility: Determine domain name from entity or use default.
     */
    private String getEntityDomainOrDefault(T entity) {
        return entity instanceof IDomainAssignable assignable
                ? assignable.getDomain()
                : DomainConstants.DEFAULT_DOMAIN_NAME;
    }

    /**
     * Utility: Apply domain rules for SAAS-based restrictions.
     */
    private void setDomainIfApplicable(String senderDomain, T entity) {
        if (IDomainAssignable.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((IDomainAssignable) entity).setDomain(senderDomain);
        }
    }

    /**
     * Utility: Handles file upload lifecycle hooks and actual storage.
     */
    private T handleFileUpload(T entity, MultipartFile file) throws IOException {
        String domain = getEntityDomainOrDefault(entity);
        entity = beforeUpload(domain, entity, file);
        subUploadFile(file, entity);
        return afterUpload(domain, entity, file);
    }
}