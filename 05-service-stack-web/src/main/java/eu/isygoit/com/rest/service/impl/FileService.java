package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.ICodeAssignableService;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * The type File service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileService<I extends Serializable, T extends IFileEntity & IIdAssignable & ICodeAssignable, R extends JpaPagingAndSortingRepository>
        extends FileServiceSubMethods<I, T, R>
        implements IFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    /**
     * Before upload t.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T beforeUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload t.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public T afterUpload(String domain, T entity, MultipartFile file) throws IOException {
        return entity;
    }

    @Transactional
    @Override
    public T createWithFile(String senderDomain, T entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (IDomainAssignable.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((IDomainAssignable) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            if (!StringUtils.hasText(entity.getCode())) {
                entity.setCode(this.getNextCode());
            }

            entity.setPath(this.getUploadDirectory() +
                    File.separator + (entity instanceof IDomainAssignable IDomainAssignable ? IDomainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                    File.separator + this.persistentClass.getSimpleName().toLowerCase());

            entity.setOriginalFileName(file.getOriginalFilename());
            entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        } else {
            log.warn("Create with file ({}) :File is null or empty", this.persistentClass.getSimpleName());
        }
        //Creating entity
        entity = this.createAndFlush(entity);

        if (file != null && !file.isEmpty()) {
            //Uploading file
            entity = this.beforeUpload((entity instanceof IDomainAssignable IDomainAssignable
                            ? IDomainAssignable.getDomain()
                            : DomainConstants.DEFAULT_DOMAIN_NAME)
                    , entity
                    , file);
            subUploadFile(file, entity);
            return this.afterUpload((entity instanceof IDomainAssignable IDomainAssignable
                            ? IDomainAssignable.getDomain()
                            : DomainConstants.DEFAULT_DOMAIN_NAME)
                    , entity
                    , file);
        }

        return entity;
    }

    @Transactional
    @Override
    public T updateWithFile(String senderDomain, I id, T entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (IDomainAssignable.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((IDomainAssignable) entity).setDomain(senderDomain);
        }

        if (repository().existsById(id)) {
            entity.setId(id);
            if (file != null && !file.isEmpty()) {
                if (!StringUtils.hasText(entity.getCode())) {
                    entity.setCode(((ICodeAssignableService) this).getNextCode());
                }

                entity.setPath(this.getUploadDirectory() +
                        File.separator + (entity instanceof IDomainAssignable IDomainAssignable ? IDomainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                        File.separator + this.persistentClass.getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            } else {
                log.warn("Update with file ({}) :File is null or empty", this.persistentClass.getSimpleName());
            }
            entity = this.updateAndFlush(entity);

            if (file != null && !file.isEmpty()) {
                //Uploading file
                entity = this.beforeUpload((entity instanceof IDomainAssignable IDomainAssignable
                                ? IDomainAssignable.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload((entity instanceof IDomainAssignable IDomainAssignable
                                ? IDomainAssignable.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
            }

            return entity;
        } else {
            throw new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id);
        }
    }

    @Transactional
    @Override
    public T uploadFile(String senderDomain, I id, MultipartFile file) throws IOException {
        Optional<T> optional = this.findById(id);
        if (optional.isPresent()) {
            T entity = optional.get();
            if (file != null && !file.isEmpty()) {
                if (!StringUtils.hasText(entity.getCode())) {
                    entity.setCode(((ICodeAssignableService) this).getNextCode());
                }

                entity.setPath(this.getUploadDirectory() +
                        File.separator + (entity instanceof IDomainAssignable IDomainAssignable ? IDomainAssignable.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                        File.separator + this.persistentClass.getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));

                entity = this.updateAndFlush(entity);

                //Uploading file
                entity = this.beforeUpload((entity instanceof IDomainAssignable IDomainAssignable
                                ? IDomainAssignable.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload((entity instanceof IDomainAssignable IDomainAssignable
                                ? IDomainAssignable.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
            } else {
                log.warn("Upload file ({}) :File is null or empty", this.persistentClass.getSimpleName());
            }

            return entity;
        } else {
            throw new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id);
        }
    }

    @Override
    public Resource downloadFile(I id, Long version) throws IOException {
        Optional<T> optional = this.findById(id);
        if (optional.isPresent()) {
            T entity = optional.get();
            return subDownloadFile(entity, version);
        } else {
            throw new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id);
        }
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
