package eu.isygoit.com.rest.service;

import eu.isygoit.constants.LogConstants;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.*;
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
import java.util.Optional;
import java.util.function.Function;

/**
 * The type File image api.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileImageService<I extends Serializable,
        T extends IImageEntity & IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>>
        extends FileService<I, T, R>
        implements IFileServiceMethods<I, T>, IImageServiceMethods<I, T> {

    private final Class<T> persistentClass =
            (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional
    public T uploadImage(I id, MultipartFile file) throws IOException {
        validateFile(file);

        T entity = findEntityByIdOrThrow(id);

        String filename = entity.getCode() + "_" + file.getOriginalFilename();
        Path path = resolveTargetPath().apply(entity);

        entity.setImagePath(FileHelper.saveMultipartFile(
                path, filename, file, "png",
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC
        ).toString());

        return update(entity);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        T entity = findEntityByIdOrThrow(id);

        if (!StringUtils.hasText(entity.getImagePath())) {
            throw new EmptyPathException("Image path is empty for ID: " + id);
        }

        Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Image not found at path: " + entity.getImagePath());
        }

        return resource;
    }

    @Override
    @Transactional
    public T createWithImage(T entity, MultipartFile file) throws IOException {
        assignCodeIfEmpty(entity);

        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename() + "_" + entity.getCode();
            Path path = resolveTargetPath().apply(entity);

            entity.setImagePath(FileHelper.saveMultipartFile(
                    path, filename, file, "png",
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC
            ).toString());
        } else {
            log.warn("Image file is null or empty for entity creation.");
        }

        return create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(T entity, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename() + "_" + entity.getCode();
            Path path = resolveTargetPath().apply(entity);

            entity.setImagePath(FileHelper.saveMultipartFile(path, filename, file, "png").toString());
        } else {
            // Retain current image path
            Optional<T> existing = findById(entity.getId());
            existing.ifPresent(e -> entity.setImagePath(e.getImagePath()));
            log.warn("Image file is null or empty for entity update.");
        }

        return update(entity);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }
    }

    private T findEntityByIdOrThrow(I id) {
        return findById(id).orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
    }

    private void assignTenantIfApplicable(String tenant, T entity) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && !TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            ((ITenantAssignable) entity).setTenant(tenant);
        }
    }

    private Function<T, Path> resolveTargetPath() {
        return entity -> {
            String tenant = (entity instanceof ITenantAssignable da)
                    ? da.getTenant()
                    : TenantConstants.DEFAULT_TENANT_NAME;

            return Path.of(getUploadDirectory())
                    .resolve(tenant)
                    .resolve(entity.getClass().getSimpleName().toLowerCase())
                    .resolve("image");
        };
    }

    protected abstract String getUploadDirectory();
}
