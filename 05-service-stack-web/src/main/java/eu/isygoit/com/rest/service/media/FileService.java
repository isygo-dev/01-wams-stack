package eu.isygoit.com.rest.service.media;

import eu.isygoit.com.rest.service.ICodeAssignableService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.StorageException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssignableRepository;
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
public abstract class FileService<I extends Serializable, T extends IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingCodeAssignableRepository<T, I>>
        extends FileServiceOperations<I, T, R>
        implements IFileServiceOperations<I, T> {

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
     * @param senderTenant the senderTenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
// Optional hooks to override before and after upload/create/update
    public T beforeUpload(String senderTenant, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload t.
     *
     * @param senderTenant the senderTenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T afterUpload(String senderTenant, T entity, MultipartFile file) throws IOException {
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
    public T createWithFile(String senderTenant, T entity, MultipartFile file) throws IOException {

        String tenant = resolveTenant(senderTenant, entity);

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        entity = beforeCreate(entity);
        entity = create(entity);
        entity = afterCreate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(tenant, entity, file);
        } else {
            log.warn("CreateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(String senderTenant, I id, T entity, MultipartFile file) throws IOException {
        T existing = repository().findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));
        entity.setId(id);

        String tenant = resolveTenant(senderTenant, entity);

        if (file != null && !file.isEmpty()) {
            assignOrPreserveCode(entity, existing);
            setFileAttributes(tenant, entity, file);
        } else {
            log.warn("UpdateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        entity = beforeUpdate(entity);
        entity = update(entity);
        entity = afterUpdate(entity);

        if (file != null && !file.isEmpty()) {
            return handleFileUpload(tenant, entity, file);
        } else {
            log.warn("UpdateWithFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        return entity;
    }

    @Transactional
    @Override
    public T uploadFile(String senderTenant, I id, MultipartFile file) throws IOException {
        T entity = findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));

        String tenant = resolveTenant(senderTenant, entity);

        if (file != null && !file.isEmpty()) {
            assignCodeIfEmpty(entity);
            setFileAttributes(tenant, entity, file);
            entity = update(entity);

            if (file != null && !file.isEmpty()) {
                return handleFileUpload(tenant, entity, file);
            } else {
                log.warn("uploadFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
            }
        } else {
            log.warn("UploadFile ({}): File is null or empty", this.getPersistentClass().getSimpleName());
        }

        return entity;
    }

    @Override
    public ResourceDto downloadFile(String senderTenant, I id, Long version) throws IOException {
        return findById(id)
                .map(entity -> {
                    try {
                        return performDownloadFile(entity, version);
                    } catch (IOException e) {
                        throw new StorageException("Failed to download file", e);
                    }
                })
                .orElseThrow(() -> new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id));
    }

    private void assignOrPreserveCode(T entity, T existing) {
        if (!StringUtils.hasText(entity.getCode()) && !StringUtils.hasText(existing.getCode())) {
            entity.setCode(((ICodeAssignableService) this).getNextCode());
        } else {
            entity.setCode(existing.getCode());
        }
    }

    private void setFileAttributes(String senderTenant, T entity, MultipartFile file) {
        Path path = Path.of(getUploadDirectory())
                .resolve(senderTenant)
                .resolve(this.getPersistentClass().getSimpleName().toLowerCase());

        entity.setPath(path.toString());
        entity.setFileName(entity.getCode() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
    }

    private String resolveTenant(String senderTenant, T entity) {
        if (entity instanceof ITenantAssignableDto tenantAssignableDto
                && StringUtils.hasText(tenantAssignableDto.getTenant())) {
            return tenantAssignableDto.getTenant();
        } else if(StringUtils.hasText(senderTenant)){
            return senderTenant;
        } else {
            return TenantConstants.DEFAULT_TENANT_NAME;
        }
    }

    private T handleFileUpload(String senderTenant, T entity, MultipartFile file) throws IOException {
        entity = beforeUpload(senderTenant, entity, file);
        performUploadFile(file, entity);
        return afterUpload(senderTenant, entity, file);
    }
}