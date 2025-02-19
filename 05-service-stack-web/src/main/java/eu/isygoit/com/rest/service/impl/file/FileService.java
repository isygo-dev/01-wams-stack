package eu.isygoit.com.rest.service.impl.file;

import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.com.rest.service.impl.file.sub.FileServiceSubMethods;
import eu.isygoit.com.rest.service.impl.utils.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
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
public abstract class FileService<E extends AssignableFile & AssignableId & AssignableCode,
        I extends Serializable,
        R extends JpaPagingAndSortingRepository<E, I>>
        extends FileServiceSubMethods<E, I, R>
        implements IFileServiceMethods<E, I> {

    /**
     * Before upload e.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the e
     * @throws IOException the io exception
     */
    public E beforeUpload(String domain, E entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload e.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the e
     * @throws IOException the io exception
     */
    public E afterUpload(String domain, E entity, MultipartFile file) throws IOException {
        return entity;
    }

    @Transactional
    @Override
    public E createWithFile(String senderDomain, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((AssignableDomain) entity).setDomain(senderDomain);
        }

        if (file != null && !file.isEmpty()) {
            entity = this.processCodeAssignable(entity);

            entity.setPath(this.getUploadDirectory() +
                    File.separator + CrudServiceUtils.getDomainOrDefault(entity) +
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
            entity = this.beforeUpload(CrudServiceUtils.getDomainOrDefault(entity),
                    entity,
                    file);
            subUploadFile(file, entity);
            return this.afterUpload(CrudServiceUtils.getDomainOrDefault(entity)
                    , entity
                    , file);
        }

        return entity;
    }

    @Transactional
    @Override
    public E updateWithFile(String senderDomain, I id, E entity, MultipartFile file) throws IOException {
        //Check SAAS entity modification
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            ((AssignableDomain) entity).setDomain(senderDomain);
        }

        if (getRepository().existsById(id)) {
            entity.setId(id);
            if (file != null && !file.isEmpty()) {
                entity = this.processCodeAssignable(entity);

                entity.setPath(this.getUploadDirectory() +
                        File.separator + CrudServiceUtils.getDomainOrDefault(entity) +
                        File.separator + this.getPersistentClass().getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            } else {
                log.warn("Update with file ({}) :File is null or empty", this.getPersistentClass().getSimpleName());
            }
            entity = this.updateAndFlush(entity);

            if (file != null && !file.isEmpty()) {
                //Uploading file
                entity = this.beforeUpload(CrudServiceUtils.getDomainOrDefault(entity),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload(CrudServiceUtils.getDomainOrDefault(entity),
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
        E entity = findById(id).get();
        if (entity != null) {
            if (file != null && !file.isEmpty()) {
                entity = this.processCodeAssignable(entity);

                entity.setPath(this.getUploadDirectory() +
                        File.separator + CrudServiceUtils.getDomainOrDefault(entity) +
                        File.separator + this.getPersistentClass().getSimpleName().toLowerCase());
                entity.setOriginalFileName(file.getOriginalFilename());
                entity.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));

                entity = this.updateAndFlush(entity);

                //Uploading file
                entity = this.beforeUpload(CrudServiceUtils.getDomainOrDefault(entity),
                        entity,
                        file);
                subUploadFile(file, entity);
                return this.afterUpload(CrudServiceUtils.getDomainOrDefault(entity),
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
        E entity = findById(id).get();
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
