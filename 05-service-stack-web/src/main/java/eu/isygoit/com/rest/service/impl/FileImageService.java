package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * The type File image service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileImageService<I extends Serializable, E extends IImageEntity & IFileEntity & IIdEntity & IAssignableCode, R extends JpaPagingAndSortingRepository>
        extends FileService<I, E, R>
        implements IFileServiceMethods<I, E>, IImageServiceMethods<I, E> {

    @Override
    @Transactional
    public E uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            E entity = this.findById(id);
            if (entity != null) {
                entity.setImagePath(FileHelper.saveMultipartFile(this.getUploadDirectory() +
                                File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                                File.separator + entity.getClass().getSimpleName().toLowerCase() +
                                File.separator + "image",
                        (entity).getCode() + "_" + file.getOriginalFilename(), file, "png").toString());
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
        E entity = this.findById(id);
        if (entity != null) {
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
        //Check SAAS entity modification
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }

        if (!StringUtils.hasText((entity).getCode())) {
            entity.setCode(this.getNextCode());
        }
        if (file != null && !file.isEmpty()) {
            entity.setImagePath(FileHelper.saveMultipartFile(this.getUploadDirectory() +
                            File.separator + (entity instanceof ISAASEntity ? ((ISAASEntity) entity).getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                            File.separator + entity.getClass().getSimpleName().toLowerCase() +
                            File.separator + "image",
                    file.getOriginalFilename() + "_" + entity.getCode(), file, "png").toString());
        } else {
            log.warn("File is null or empty");
        }
        return this.create(entity);
    }

    @Override
    @Transactional
    public E updateWithImage(String senderDomain, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            entity.setImagePath(FileHelper.saveMultipartFile(this.getUploadDirectory() +
                            File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                            File.separator + entity.getClass().getSimpleName().toLowerCase() +
                            File.separator + "image",
                    file.getOriginalFilename() + "_" + entity.getCode(), file, "png").toString());
        } else {
            entity.setImagePath(this.findById((I) entity.getId()).getImagePath());
            log.warn("File is null or empty");
        }
        return this.update(entity);
    }

    protected abstract String getUploadDirectory();
}
