package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.com.rest.service.ICodeAssignableService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
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
public abstract class FileTenantService<I extends Serializable,
        T extends IFileEntity & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>>
        extends FileTenantServiceSubMethods<I, T, R>
        implements IFileTenantServiceMethods<I, T> {

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

    /**
     * Before create t.
     *
     * @param object the object
     * @return the t
     */
    public T beforeCreate(T object) {
        return object;
    }

    /**
     * After create t.
     *
     * @param object the object
     * @return the t
     */
    public T afterCreate(T object) {
        return object;
    }

    /**
     * Before update t.
     *
     * @param object the object
     * @return the t
     */
    public T beforeUpdate(T object) {
        return object;
    }

    /**
     * After update t.
     *
     * @param object the object
     * @return the t
     */
    public T afterUpdate(T object) {
        return object;
    }

    @Transactional
    @Override
    public T createWithFile(String tenant, T entity, MultipartFile file) throws IOException {
        setTenantIfApplicable(tenant, entity);

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        entity = beforeCreate(entity);
        entity = create(tenant, entity);
        entity = afterCreate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(tenant, entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(String tenant, I id, T entity, MultipartFile file) throws IOException {
        setTenantIfApplicable(tenant, entity);

        T existing = repository().findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
        entity.setId(id);

        if (file != null && !file.isEmpty()) {
            assignOrPreserveCode(entity, existing);
            setFileAttributes(tenant, entity, file);
        } else {
            log.warn("UpdateWithFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        entity = beforeUpdate(entity);
        entity = update(tenant, entity);
        entity = afterUpdate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(tenant, entity, file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T uploadFile(String tenant, I id, MultipartFile file) throws IOException {
        T entity = findById(tenant, id)
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file);
            entity = update(tenant, entity);

            return handleFileUpload(tenant, entity, file);
        } else {
            log.warn("UploadFile ({}): File is null or empty", persistentClass.getSimpleName());
        }

        return entity;
    }

    @Override
    public Resource downloadFile(String tenant, I id, Long version) throws IOException {
        return findById(tenant, id)
                .map(entity -> subDownloadFile(entity, version))
                .orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
    }

    private void assignOrPreserveCode(T entity, T existing) {
        if (!StringUtils.hasText(entity.getCode()) && !StringUtils.hasText(existing.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        } else {
            entity.setCode(existing.getCode());
        }
    }

    private void setFileAttributes(String tenant, T entity, MultipartFile file) {
        Path path = Path.of(getUploadDirectory())
                .resolve(tenant)
                .resolve(persistentClass.getSimpleName().toLowerCase());

        entity.setPath(path.toString());
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
    }

    private void setTenantIfApplicable(String tenant, T entity) {
        if (!TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            entity.setTenant(tenant);
        }
    }

    private T handleFileUpload(String tenant, T entity, MultipartFile file) throws IOException {
        entity = beforeUpload(tenant, entity, file);
        subUploadFile(file, entity);
        return afterUpload(tenant, entity, file);
    }
}