package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.IAssignableCodeService;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.IAssignableCode;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
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

/**
 * The type File service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileService<I extends Serializable, E extends IFileEntity & IIdEntity & IAssignableCode, R extends JpaPagingAndSortingRepository>
        extends FileServiceSubMethods<I, E, R>
        implements IFileServiceMethods<I, E> {

    /**
     * Before upload t.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    public E beforeUpload(String domain, E entity, MultipartFile file) throws IOException {
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
    public E afterUpload(String domain, E entity, MultipartFile file) throws IOException {
        return entity;
    }

    @Transactional
    @Override
    public E createWithFile(String senderDomain, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            if (!StringUtils.hasText(entity.getCode())) {
                entity.setCode(this.getNextCode());
            }

            entity.setPath(this.getUploadDirectory() +
                    File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                    File.separator + this.getPersistentClass().getSimpleName().toLowerCase());

            entity.setOriginalFileName(file.getOriginalFilename());
            entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        } else {
            log.warn("Create with file ({}) :File is null or empty", this.getPersistentClass().getSimpleName());
        }
        //Creating entity
        entity = this.createAndFlush(entity);

        if (file != null && !file.isEmpty()) {
            //Uploading file
            entity = this.beforeUpload((entity instanceof ISAASEntity isaasEntity
                            ? isaasEntity.getDomain()
                            : DomainConstants.DEFAULT_DOMAIN_NAME)
                    , entity
                    , file);
            subUploadFile(file, entity);
            return this.afterUpload((entity instanceof ISAASEntity isaasEntity
                            ? isaasEntity.getDomain()
                            : DomainConstants.DEFAULT_DOMAIN_NAME)
                    , entity
                    , file);
        }

        return entity;
    }

    @Transactional
    @Override
    public E updateWithFile(String senderDomain, I id, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((ISAASEntity) entity).setDomain(senderDomain);
        }

        if (repository().existsById(id)) {
            entity.setId(id);
            if (file != null && !file.isEmpty()) {
                if (!StringUtils.hasText(entity.getCode())) {
                    entity.setCode(((IAssignableCodeService) this).getNextCode());
                }

                entity.setPath(this.getUploadDirectory() +
                        File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                        File.separator + this.getPersistentClass().getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            } else {
                log.warn("Update with file ({}) :File is null or empty", this.getPersistentClass().getSimpleName());
            }
            entity = this.updateAndFlush(entity);

            if (file != null && !file.isEmpty()) {
                //Uploading file
                entity = this.beforeUpload((entity instanceof ISAASEntity isaasEntity
                                ? isaasEntity.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload((entity instanceof ISAASEntity isaasEntity
                                ? isaasEntity.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
            }

            return entity;
        } else {
            throw new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id);
        }
    }

    @Transactional
    @Override
    public E uploadFile(String senderDomain, I id, MultipartFile file) throws IOException {
        E entity = findById(id);
        if (entity != null) {
            if (file != null && !file.isEmpty()) {
                if (!StringUtils.hasText(entity.getCode())) {
                    entity.setCode(((IAssignableCodeService) this).getNextCode());
                }

                entity.setPath(this.getUploadDirectory() +
                        File.separator + (entity instanceof ISAASEntity isaasEntity ? isaasEntity.getDomain() : DomainConstants.DEFAULT_DOMAIN_NAME) +
                        File.separator + this.getPersistentClass().getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));

                entity = this.updateAndFlush(entity);

                //Uploading file
                entity = this.beforeUpload((entity instanceof ISAASEntity isaasEntity
                                ? isaasEntity.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload((entity instanceof ISAASEntity isaasEntity
                                ? isaasEntity.getDomain()
                                : DomainConstants.DEFAULT_DOMAIN_NAME),
                        entity,
                        file);
            } else {
                log.warn("Upload file ({}) :File is null or empty", this.getPersistentClass().getSimpleName());
            }

            return entity;
        } else {
            throw new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id);
        }
    }

    @Override
    public Resource downloadFile(I id, Long version) throws IOException {
        E entity = findById(id);
        if (entity != null) {
            return subDownloadFile(entity, version);
        } else {
            throw new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + id);
        }
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
