package eu.isygoit.com.rest.service;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
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

/**
 * The type File api.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileService<I extends Serializable, T extends IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>>
        extends FileServiceSubMethods<I, T, R>
        implements IFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    /**
     * Before upload t.
     *
     * @param tenant the tenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
// Optional hooks to override before and after upload/create/update
    public T beforeUpload(String tenant, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload t.
     *
     * @param tenant the tenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T afterUpload(String tenant, T entity, MultipartFile file) throws IOException {
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
    public T createWithFile(T entity, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(entity, file);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        entity = beforeCreate(entity);
        entity = create(entity);
        entity = afterCreate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(I id, T entity, MultipartFile file) throws IOException {
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
        entity = update(entity);
        entity = afterUpdate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T uploadFile(I id, MultipartFile file) throws IOException {
        T entity = findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(entity, file);
            entity = update(entity);

            return handleFileUpload(entity, file);
        } else {
            log.warn("UploadFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        return entity;
    }

    @Override
    public Resource downloadFile(I id, Long version) throws IOException {
        return findById(id)
                .map(entity -> {
                    try {
                        return subDownloadFile(entity, version);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
    }

    private void assignOrPreserveCode(T entity, T existing) {
        if (!StringUtils.hasText(entity.getCode()) && !StringUtils.hasText(existing.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        } else {
            entity.setCode(existing.getCode());
        }
    }

    private void setFileAttributes(T entity, MultipartFile file) {
        Path path = Path.of(getUploadDirectory())
                .resolve(getEntityTenantOrDefault(entity))
                .resolve(persistentClass.getSimpleName().toLowerCase());

        entity.setPath(path.toString());
        entity.setFileName(entity.getCode() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
    }

    private String getEntityTenantOrDefault(T entity) {
        return entity instanceof ITenantAssignable assignable
                ? assignable.getTenant()
                : TenantConstants.DEFAULT_TENANT_NAME;
    }

    private void setTenantIfApplicable(String tenant, T entity) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            ((ITenantAssignable) entity).setTenant(tenant);
        }
    }

    private T handleFileUpload(T entity, MultipartFile file) throws IOException {
        String tenant = getEntityTenantOrDefault(entity);
        entity = beforeUpload(tenant, entity, file);
        subUploadFile(file, entity);
        return afterUpload(tenant, entity, file);
    }
}