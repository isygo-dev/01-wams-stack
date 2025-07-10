package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.com.rest.controller.impl.tenancy.IImageTenantServiceMethods;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * The type Image api.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class ImageTenantService<I extends Serializable,
        T extends IImageEntity & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>>
        extends CodeAssignableTenantService<I, T, R>
        implements IImageTenantServiceMethods<I, T> {

    // Persistent class derived via reflection for exception messages etc.
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];

    private String saveImageFile(T entity, MultipartFile file) throws IOException {
        // Determine target directory based on entity tenant and class name
        Path target = Path.of(getUploadDirectory())
                .resolve(entity.getTenant())
                .resolve(entity.getClass().getSimpleName().toLowerCase())
                .resolve("image");

        // Save the file and return the path as string
        return FileHelper.saveMultipartFile(target,
                        file.getOriginalFilename() + "_" + entity.getCode(),
                        file, "png",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.SYNC)
                .toString();
    }

    @Override
    @Transactional
    public T uploadImage(String tenant, I id, MultipartFile file) throws IOException {
        // Validate input file
        if (file == null || file.isEmpty()) {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }

        // Retrieve entity by id or throw exception
        T entity = findById(tenant, id).orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        // Save image and update entity path
        entity.setImagePath(saveImageFile(entity, file));
        return update(tenant, entity);
    }

    @Override
    public Resource downloadImage(String tenant, I id) throws IOException {
        // Retrieve entity or throw exception if not found
        T entity = findById(tenant, id).orElseThrow(() ->
                new ResourceNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        // Validate image path
        if (!StringUtils.hasText(entity.getImagePath())) {
            throw new EmptyPathException(persistentClass.getSimpleName() + " with id " + id);
        }

        Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());

        // Check resource existence
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Resource not found for path " + entity.getImagePath());
        }
        return resource;
    }

    @Override
    @Transactional
    public T createWithImage(String tenant, T entity, MultipartFile file) throws IOException {
        // Enforce tenant if applicable and not super tenant
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            ((ITenantAssignable) entity).setTenant(tenant);
        }

        // Assign code if empty
        assignCodeIfEmpty(entity);

        // Save image if provided
        if (file != null && !file.isEmpty()) {
            entity.setImagePath(saveImageFile(entity, file));
        } else {
            log.warn("File is null or empty");
        }
        return create(tenant, entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String tenant, T entity, MultipartFile file) throws IOException {
        // Enforce tenant if applicable and not super tenant
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            ((ITenantAssignable) entity).setTenant(tenant);
        }

        if (file != null && !file.isEmpty()) {
            // Save new image and update path
            entity.setImagePath(saveImageFile(entity, file));
        } else {
            // Keep existing image path if no new file provided
            String existingPath = findById(tenant, entity.getId())
                    .map(T::getImagePath)
                    .orElse(null);
            entity.setImagePath(existingPath);
            log.warn("File is null or empty");
        }
        return update(tenant, entity);
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    // Lifecycle hooks for subclasses to override if needed

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
}