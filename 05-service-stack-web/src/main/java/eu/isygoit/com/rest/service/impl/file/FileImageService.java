package eu.isygoit.com.rest.service.impl.file;

import com.datastax.oss.driver.shaded.guava.common.io.Files;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.com.rest.service.impl.utils.CrudServiceUtils;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableFile;
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
import java.util.Optional;

/**
 * The type File image service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileImageService<I extends Serializable, E extends AssignableImage & AssignableFile & AssignableId & AssignableCode, R extends JpaPagingAndSortingRepository<I, E>>
        extends FileService<I, E, R>
        implements IFileServiceMethods<I, E>, IImageServiceMethods<I, E> {

    @Override
    @Transactional
    public E uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            Optional<E> optional = this.findById(id);
            if (optional.isPresent()) {
                E entity = optional.get();
                entity.setImagePath(FileHelper.saveMultipartFile(Path.of(this.getUploadDirectory())
                                .resolve(CrudServiceUtils.getDomainOrDefault(entity))
                                .resolve(entity.getClass().getSimpleName().toLowerCase())
                                .resolve("image"),
                        (entity).getCode() + "_" + file.getOriginalFilename(), file, Files.getFileExtension(file.getOriginalFilename())).toString());
                return this.update(entity);
            } else {
                throw new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id);
            }
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
        }

        throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        Optional<E> optional = this.findById(id);
        if (optional.isPresent()) {
            E entity = optional.get();
            if (StringUtils.hasText(entity.getImagePath())) {
                Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
                if (!resource.exists()) {
                    throw new ResourceNotFoundException("for path " + entity.getImagePath());
                }
                return resource;
            } else {
                throw new EmptyPathException("for id " + id);
            }
        } else {
            throw new ResourceNotFoundException("with id " + id);
        }
    }

    @Override
    @Transactional
    public E createWithImage(String senderDomain, E entity, MultipartFile file) throws IOException {
        entity = this.processDomainAssignable(senderDomain, entity);
        entity = this.processCodeAssignable(entity);

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
        entity = this.processDomainAssignable(senderDomain, entity);
        entity = this.processCodeAssignable(entity);

        if (file != null && !file.isEmpty()) {
            entity.setImagePath(FileHelper.saveMultipartFile(Path.of(this.getUploadDirectory())
                            .resolve(CrudServiceUtils.getDomainOrDefault(entity))
                            .resolve(entity.getClass().getSimpleName().toLowerCase())
                            .resolve("image"),
                    file.getOriginalFilename() + "_" + entity.getCode(), file, Files.getFileExtension(file.getOriginalFilename())).toString());
        } else {
            Optional<E> optional = this.findById((I) entity.getId());
            if (optional.isPresent()) {
                E e = optional.get();
                entity.setImagePath(e.getImagePath());
            }
            log.warn("File is null or empty");
        }
        return this.update(entity);
    }

    protected abstract String getUploadDirectory();
}
