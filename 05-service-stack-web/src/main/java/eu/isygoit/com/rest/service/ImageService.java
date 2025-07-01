package eu.isygoit.com.rest.service;

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
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
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
 * The type Image service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class ImageService<I extends Serializable, T extends IImageEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>>
        extends CodeAssignableService<I, T, R>
        implements IImageServiceMethods<I, T> {

    // Persistent class derived via reflection for exception messages etc.
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];

    private String saveImageFile(T entity, MultipartFile file) throws IOException {
        // Determine target directory based on entity tenant and class name
        Path target = Path.of(getUploadDirectory())
                .resolve(entity instanceof ITenantAssignable tenantAssignable
                        ? tenantAssignable.getTenant() : TenantConstants.DEFAULT_TENANT_NAME)
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
    public T uploadImage(String senderTenant, I id, MultipartFile file) throws IOException {
        // Validate input file
        if (file == null || file.isEmpty()) {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }

        // Retrieve entity by id or throw exception
        T entity = findById(id).orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));

        // Save image and update entity path
        entity.setImagePath(saveImageFile(entity, file));
        return update(entity);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        // Retrieve entity or throw exception if not found
        T entity = findById(id).orElseThrow(() ->
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
    public T createWithImage(String senderTenant, T entity, MultipartFile file) throws IOException {
        // Enforce tenant if applicable and not super tenant
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(senderTenant)) {
            ((ITenantAssignable) entity).setTenant(senderTenant);
        }

        // Assign code if empty
        assignCodeIfEmpty(entity);

        // Save image if provided
        if (file != null && !file.isEmpty()) {
            entity.setImagePath(saveImageFile(entity, file));
        } else {
            log.warn("File is null or empty");
        }
        return create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String senderTenant, T entity, MultipartFile file) throws IOException {
        // Enforce tenant if applicable and not super tenant
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(senderTenant)) {
            ((ITenantAssignable) entity).setTenant(senderTenant);
        }

        if (file != null && !file.isEmpty()) {
            // Save new image and update path
            entity.setImagePath(saveImageFile(entity, file));
        } else {
            // Keep existing image path if no new file provided
            String existingPath = findById(entity.getId())
                    .map(T::getImagePath)
                    .orElse(null);
            entity.setImagePath(existingPath);
            log.warn("File is null or empty");
        }
        return update(entity);
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    // Lifecycle hooks for subclasses to override if needed

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