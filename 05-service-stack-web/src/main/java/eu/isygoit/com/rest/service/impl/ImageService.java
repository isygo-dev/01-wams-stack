package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.*;
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

@FunctionalInterface
interface ImagePathGenerator<T extends IImageEntity> {
    /**
     * Generates the image path for the uploaded file.
     * @param entity the entity for which the image path is generated
     * @param file the uploaded file
     * @return the generated image path
     * @throws IOException if file operation fails
     */
    String generate(T entity, MultipartFile file) throws IOException;
}

@FunctionalInterface
interface EntityProcessor<T> {
    /**
     * Processes the entity (e.g., setting domain or code).
     * @param entity the entity to be processed
     */
    void process(T entity);
}

@Slf4j
public abstract class ImageService<I, T extends IImageEntity & IIdEntity, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R> implements IImageServiceMethods<I, T> {

    // Persistent class type for reflection purposes
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    // Path generator for storing the uploaded image
    private final ImagePathGenerator<T> imagePathGenerator = (entity, file) ->
            FileHelper.storeMultipartFile(
                    getUploadDirectory() +
                            File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                            File.separator + entity.getClass().getSimpleName().toLowerCase() +
                            File.separator + "image",
                    file.getOriginalFilename() + "_" + (entity instanceof ICodifiable codifiable ? codifiable.getCode() : entity.getId()),
                    file, "png").toString();

    // Sets the domain for SAAS entities
    private final EntityProcessor<T> saasDomainSetter = (entity) -> {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)) {
            ((ISAASEntity) entity).setDomain(DomainConstants.DEFAULT_DOMAIN_NAME);
            log.info("Setting domain to default for entity: {}", entity);
        }
    };

    // Sets the code for codifiable entities
    private final EntityProcessor<T> codifiableCodeSetter = (entity) -> {
        if (entity instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            getNextCode().ifPresent(codifiable::setCode);
            log.info("Setting code for codifiable entity: {}", codifiable);
        }
    };

    @Override
    @Transactional
    public T uploadImage(String senderDomain, I id, MultipartFile file) throws IOException {
        if (Objects.nonNull(file) && !file.isEmpty()) {
            log.info("Uploading image for entity with id: {}", id);
            T entity = this.getById(id).orElseThrow(() -> new ObjectNotFoundException(persistentClass.getSimpleName() + " with id " + id));
            entity.setImagePath(imagePathGenerator.generate(entity, file));
            return this.update(entity);
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }
    }

    @Override
    public Resource downloadImage(I id) throws IOException {
        log.info("Downloading image for entity with id: {}", id);
        T entity = this.getById(id).orElseThrow(() -> new ResourceNotFoundException(persistentClass.getSimpleName() + " with id " + id));
        if (StringUtils.hasText(entity.getImagePath())) {
            Resource resource = new UrlResource(Path.of(entity.getImagePath()).toUri());
            if (!resource.exists()) {
                log.error("Image file not found at path: {}", entity.getImagePath());
                throw new ResourceNotFoundException("for path " + entity.getImagePath());
            }
            return resource;
        } else {
            log.error("No image path set for entity with id: {}", id);
            throw new EmptyPathException(persistentClass.getSimpleName() + " for id " + id);
        }
    }

    @Override
    @Transactional
    public T createWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        log.info("Creating entity with image");
        saasDomainSetter.process(entity); // Set domain if applicable
        codifiableCodeSetter.process(entity); // Set code if applicable

        if (Objects.nonNull(file) && !file.isEmpty()) {
            log.info("Processing image upload for entity: {}", entity);
            entity.setImagePath(imagePathGenerator.generate(entity, file));
        } else {
            log.warn("File is null or empty for entity: {}", entity);
        }
        return this.create(entity);
    }

    @Override
    @Transactional
    public T updateWithImage(String senderDomain, T entity, MultipartFile file) throws IOException {
        log.info("Updating entity with id: {}", entity.getId());
        saasDomainSetter.process(entity); // Set domain if applicable

        if (Objects.nonNull(file) && !file.isEmpty()) {
            log.info("Processing image upload for entity: {}", entity);
            entity.setImagePath(imagePathGenerator.generate(entity, file));
        } else {
            // If no file is provided, retain existing image path
            this.getById((I) entity.getId()).ifPresent(existingEntity -> {
                entity.setImagePath(existingEntity.getImagePath());
                log.info("Retaining existing image path for entity with id: {}", entity.getId());
            });
            log.warn("File is null or empty for entity: {}", entity);
        }
        return this.update(entity);
    }

    /**
     * Returns the directory where the uploaded files should be stored.
     * @return the upload directory path
     */
    protected abstract String getUploadDirectory();
}