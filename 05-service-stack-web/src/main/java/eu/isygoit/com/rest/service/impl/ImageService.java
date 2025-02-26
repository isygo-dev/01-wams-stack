package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
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
import java.util.Optional;

/**
 * The type Image service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class ImageService<I extends Serializable, T extends IImageEntity & IIdAssignable, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R>
        implements IImageServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional
    public T uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            Optional<T> optional = findById(id);
            if (optional.isPresent()) {
                T entity = optional.get();
                Path target = Path.of(this.getUploadDirectory())
                        .resolve(entity instanceof IDomainAssignable domainAssignable ? domainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME)
                        .resolve(entity.getClass().getSimpleName().toLowerCase())
                        .resolve("image");
                entity.setImagePath(FileHelper.saveMultipartFile(target,
                        file.getOriginalFilename() + "_" + (entity instanceof ICodeAssignable codeAssignable ? codeAssignable.getCode() : entity.getId()), file, "png").toString());
                return this.update(entity);
            } else {
                throw new ObjectNotFoundException(persistentClass.getSimpleName() + "with id " + id);
            }
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
        }

        throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        Optional<T> optional = findById(id);
        if (optional.isPresent()) {
            T entity = optional.get();
            if (StringUtils.hasText(entity.getImagePath())) {
                Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
                if (!resource.exists()) {
                    throw new ResourceNotFoundException("for path " + entity.getImagePath());
                }
                return resource;
            } else {
                throw new EmptyPathException(persistentClass.getSimpleName() + "for id " + id);
            }
        } else {
            throw new ResourceNotFoundException(persistentClass.getSimpleName() + "with id " + id);
        }
    }

    @Override
    @Transactional
    public T createWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (IDomainAssignable.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((IDomainAssignable) entity).setDomain(senderDomain);
        }

        if (entity instanceof ICodeAssignable codeAssignable && !StringUtils.hasText(codeAssignable.getCode())) {
            ((ICodeAssignable) entity).setCode(this.getNextCode());
        }
        if (file != null && !file.isEmpty()) {
            Path target = Path.of(this.getUploadDirectory())
                    .resolve(entity instanceof IDomainAssignable domainAssignable ? domainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME)
                    .resolve(entity.getClass().getSimpleName().toLowerCase())
                    .resolve("image");
            entity.setImagePath(FileHelper.saveMultipartFile(target,
                    file.getOriginalFilename() + "_" + (entity instanceof ICodeAssignable codeAssignable ? codeAssignable.getCode() : entity.getId()), file, "png").toString());
        } else {
            log.warn("File is null or empty");
        }
        return this.create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (IDomainAssignable.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((IDomainAssignable) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            Path target = Path.of(this.getUploadDirectory())
                    .resolve(entity instanceof IDomainAssignable domainAssignable ? domainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME)
                    .resolve(entity.getClass().getSimpleName().toLowerCase())
                    .resolve("image");
            entity.setImagePath(FileHelper.saveMultipartFile(target,
                    file.getOriginalFilename() + "_" + (entity instanceof ICodeAssignable codeAssignable ? codeAssignable.getCode() : entity.getId()), file, "png").toString());
        } else {
            entity.setImagePath(this.findById((I) entity.getId()).get().getImagePath());
            log.warn("File is null or empty");
        }
        return this.update(entity);
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

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
