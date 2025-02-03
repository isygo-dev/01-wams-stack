package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ISAASEntity;
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
 * The type Image service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class ImageService<I, T extends IImageEntity & IIdEntity, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R> implements IImageServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional
    public T uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (Objects.nonNull(file) && !file.isEmpty()) {
            T entity = this.findById(id).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
            String imagePath = generateImagePath(entity, file);
            entity.setImagePath(imagePath);
            return this.update(entity);
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        T entity = this.findById(id).orElseThrow(() -> new ResourceNotFoundException(persistentClass.getSimpleName() + " with id " + id));
        if (StringUtils.hasText(entity.getImagePath())) {
            Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("for path " + entity.getImagePath());
            }
            return resource;
        } else {
            throw new EmptyPathException(persistentClass.getSimpleName() + " for id " + id);
        }
    }

    @Override
    @Transactional
    public T createWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        // Set domain for SAAS entity if applicable
        setDomainForSaaSEntity(senderDomain, entity);

        // Set code if entity is codifiable
        setCodeForEntityIfNeeded(entity);

        if (Objects.nonNull(file) && !file.isEmpty()) {
            String imagePath = generateImagePath(entity, file);
            entity.setImagePath(imagePath);
        } else {
            log.warn("File is null or empty");
        }

        return this.create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        // Set domain for SAAS entity if applicable
        setDomainForSaaSEntity(senderDomain, entity);

        if (Objects.nonNull(file) && !file.isEmpty()) {
            String imagePath = generateImagePath(entity, file);
            entity.setImagePath(imagePath);
        } else {
            // Preserve existing image path if no file is provided
            this.findById((I) entity.getId()).ifPresent(object -> entity.setImagePath(object.getImagePath()));
            log.warn("File is null or empty");
        }

        return this.update(entity);
    }

    // Helper method to generate the image path
    private String generateImagePath(T entity, MultipartFile file) throws IOException {
        return FileHelper.storeMultipartFile(
                this.getUploadDirectory() +
                        File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                        File.separator + entity.getClass().getSimpleName().toLowerCase() +
                        File.separator + "image",
                file.getOriginalFilename() + "_" + (entity instanceof ICodifiable codifiable ? codifiable.getCode() : entity.getId()),
                file, "png").toString();
    }

    // Helper method to set domain for SAAS entities
    private void setDomainForSaaSEntity(String senderDomain, T entity) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }
    }

    // Helper method to set code if the entity is codifiable
    private void setCodeForEntityIfNeeded(T entity) {
        if (entity instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            this.getNextCode().ifPresent(code -> codifiable.setCode(code));
        }
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();

    // Hook methods for pre- and post-processing
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
