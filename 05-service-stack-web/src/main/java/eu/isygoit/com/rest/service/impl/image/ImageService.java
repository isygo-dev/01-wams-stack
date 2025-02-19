package eu.isygoit.com.rest.service.impl.image;

import com.datastax.oss.driver.shaded.guava.common.io.Files;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.com.rest.service.impl.crud.AssignableCodeService;
import eu.isygoit.com.rest.service.impl.utils.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.AssignableImage;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * The type Image service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class ImageService<I extends Serializable, E extends AssignableImage & AssignableId & AssignableCode, R extends JpaPagingAndSortingRepository<E, I>>
        extends AssignableCodeService<E, I, R>
        implements IImageServiceMethods<E, I> {

    @Override
    @Transactional
    public E uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            E entity = this.findById(id).get();
            if (entity != null) {
                entity.setImagePath(FileHelper.saveMultipartFile(Path.of(this.getUploadDirectory())
                                .resolve(CrudServiceUtils.getDomainOrDefault(entity))
                                .resolve(entity.getClass().getSimpleName().toLowerCase())
                                .resolve("image"),
                        file.getOriginalFilename() + "_" + entity.getCode(), file, Files.getFileExtension(file.getOriginalFilename())).toString());
                return this.update(entity);
            } else {
                throw new ObjectNotFoundException(getPersistentClass().getSimpleName() + "with id " + id);
            }
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
        }

        throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        E entity = this.findById(id).get();
        if (entity != null) {
            if (StringUtils.hasText(entity.getImagePath())) {
                Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
                if (!resource.exists()) {
                    throw new ResourceNotFoundException("for path " + entity.getImagePath());
                }
                return resource;
            } else {
                throw new EmptyPathException(getPersistentClass().getSimpleName() + "for id " + id);
            }
        } else {
            throw new ResourceNotFoundException(getPersistentClass().getSimpleName() + "with id " + id);
        }
    }

    @Override
    @Transactional
    public E createWithImage(String senderDomain, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((AssignableDomain) entity).setDomain(senderDomain);
        }

        this.processCodeAssignable(entity);

        if (file != null && !file.isEmpty()) {
            entity.setImagePath(FileHelper.saveMultipartFile(Path.of(this.getUploadDirectory())
                            .resolve(CrudServiceUtils.getDomainOrDefault(entity))
                            .resolve(entity.getClass().getSimpleName().toLowerCase())
                            .resolve("image"),
                    file.getOriginalFilename() + "_" + entity.getCode(), file, Files.getFileExtension(file.getOriginalFilename())).toString());
        } else {
            log.warn("File is null or empty");
        }
        return this.create(entity);
    }

    @Override
    @Transactional
    public E updateWithImage(String senderDomain, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((AssignableDomain) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            entity.setImagePath(FileHelper.saveMultipartFile(Path.of(this.getUploadDirectory())
                            .resolve(CrudServiceUtils.getDomainOrDefault(entity))
                            .resolve(entity.getClass().getSimpleName().toLowerCase())
                            .resolve("image"),
                    file.getOriginalFilename() + "_" + entity.getCode(), file, Files.getFileExtension(file.getOriginalFilename())).toString());
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

    public E beforeUpdate(E object) {
        return object;
    }

    public E afterUpdate(E object) {
        return object;
    }

    public E beforeCreate(E object) {
        return object;
    }

    public E afterCreate(E object) {
        return object;
    }
}
