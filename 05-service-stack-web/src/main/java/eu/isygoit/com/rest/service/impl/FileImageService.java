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
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The type File image service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileImageService<I, T extends IImageEntity & IFileEntity & IIdEntity & ICodifiable, R extends JpaPagingAndSortingRepository>
        extends FileService<I, T, R> implements IFileServiceMethods<I, T>, IImageServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional
    public T uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        validateFile(file);
        T entity = this.findById(id).orElseThrow(() -> new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id));
        entity.setImagePath(storeImage(file, entity));
        return this.update(entity);
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        T entity = this.findById(id).orElseThrow(() -> new ResourceNotFoundException("with id " + id));
        if (StringUtils.hasText(entity.getImagePath())) {
            Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("for path " + entity.getImagePath());
            }
            return resource;
        } else {
            throw new EmptyPathException("for id " + id);
        }
    }

    @Override
    @Transactional
    public T createWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        setDomainForSaaSEntity(senderDomain, entity);
        setCodeForEntityIfNeeded(entity);
        if (Objects.nonNull(file) && !file.isEmpty()) {
            entity.setImagePath(storeImage(file, entity));
        } else {
            log.warn("File is null or empty");
        }
        return this.create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        setDomainForSaaSEntity(senderDomain, entity);
        if (Objects.nonNull(file) && !file.isEmpty()) {
            entity.setImagePath(storeImage(file, entity));
        } else {
            this.findById((I) entity.getId()).ifPresent(object -> entity.setImagePath(object.getImagePath()));
            log.warn("File is null or empty");
        }
        return this.update(entity);
    }

    // Helper methods to centralize logic
    private void validateFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }
    }

    private void setDomainForSaaSEntity(String senderDomain, T entity) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }
    }

    private void setCodeForEntityIfNeeded(T entity) {
        if (!StringUtils.hasText(entity.getCode())) {
            this.getNextCode().ifPresent(code -> entity.setCode(code));
        }
    }

    private String storeImage(MultipartFile file, T entity) throws IOException {
        String directory = this.getUploadDirectory() +
                File.separator + (entity instanceof ISAASEntity ? ((ISAASEntity) entity).getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                File.separator + entity.getClass().getSimpleName().toLowerCase() +
                File.separator + "image";

        return FileHelper.storeMultipartFile(directory, entity.getCode() + "_" + file.getOriginalFilename(), file, "png").toString();
    }

    protected abstract String getUploadDirectory();
}