package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.com.rest.service.ICodeAssignableService;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.StorageException;
import eu.isygoit.helper.CRC16Helper;
import eu.isygoit.helper.CRC32Helper;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ISingleFileEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
        T extends ISingleFileEntity & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>>
        extends FileTenantServiceOperations<I, T, R>
        implements IFileTenantServiceOperations<I, T> {

    @Getter
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
        entity = TenantHelper.assignTenantIfApplicable(tenant, entity);

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file, null);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
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
        entity = TenantHelper.assignTenantIfApplicable(tenant, entity);

        T existing = repository().findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));
        entity.setId(id);

        if (file != null && !file.isEmpty()) {
            assignOrPreserveCode(entity, existing);
            setFileAttributes(tenant, entity, file, existing.getVersion());
        } else {
            log.warn("UpdateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
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
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file, entity.getVersion());
            entity = update(tenant, entity);

            return handleFileUpload(tenant, entity, file);
        } else {
            log.warn("UploadFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        return entity;
    }

    @Override
    public ResourceDto downloadFile(String tenant, I id, Long version) throws IOException {
        return findById(tenant, id)
                .map(entity -> {
                    try {
                        return performDownloadFile(entity, version);
                    } catch (IOException e) {
                        throw new StorageException("Failed to download file", e);
                    }
                })
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));
    }

    @Transactional
    @Override
    public T updateFileCategory(String tenant, I id, String category) {
        T entity = findById(tenant, id)
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));
        entity.setFileCategory(StringUtils.hasText(category) ? category.trim() : null);
        return update(tenant, entity);
    }

    @Transactional
    @Override
    public T deleteFile(String tenant, I id) throws IOException {
        T entity = findById(tenant, id)
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));

        if (StringUtils.hasText(entity.getFileName()) && StringUtils.hasText(entity.getPath())) {
            performDeleteFile(entity);
        }
        entity.setFileName(null);
        entity.setOriginalFileName(null);
        entity.setSize(null);
        entity.setVersion(null);
        entity.setCrc16(null);
        entity.setCrc32(null);
        entity.setExtension(null);
        entity.setMimetype(null);
        entity.setPath(null);
        return update(tenant, entity);
    }

    private void assignOrPreserveCode(T entity, T existing) {
        if (!StringUtils.hasText(entity.getCode()) && !StringUtils.hasText(existing.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        } else {
            entity.setCode(existing.getCode());
        }
    }

    private void setFileAttributes(String tenant, T entity, MultipartFile file, Long previousVersion) throws IOException {
        Path path = Path.of(getUploadDirectory())
                .resolve(tenant)
                .resolve(this.getPersistentClass().getSimpleName().toLowerCase());

        entity.setPath(path.toString());
        entity.setFileName(entity.getCode() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        entity.setMimetype(file.getContentType());
        // size, checksums and version of the stored file; a replaced file is the next version
        byte[] content = file.getBytes();
        entity.setSize(file.getSize());
        entity.setCrc16(CRC16Helper.calculate(content));
        entity.setCrc32(CRC32Helper.calculate(content));
        entity.setVersion(previousVersion == null ? 1L : previousVersion + 1);
    }

    private T handleFileUpload(String tenant, T entity, MultipartFile file) throws IOException {
        entity = beforeUpload(tenant, entity, file);
        performUploadFile(file, entity);
        return afterUpload(tenant, entity, file);
    }
}