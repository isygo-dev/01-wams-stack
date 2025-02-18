package eu.isygoit.com.rest.service.impl.file;

import eu.isygoit.com.rest.service.IMultiFileServiceMethods;
import eu.isygoit.com.rest.service.impl.file.sub.MultiFileServiceSubMethods;
import eu.isygoit.com.rest.service.impl.utils.CrudServiceUtils;
import eu.isygoit.encrypt.helper.CRC16Helper;
import eu.isygoit.encrypt.helper.CRC32Helper;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Multi file service.
 *
 * @param <I>  the type parameter
 * @param <E>  the type parameter
 * @param <L>  the type parameter
 * @param <R>  the type parameter
 * @param <RL> the type parameter
 */
@Slf4j
public abstract class MultiFileService<I extends Serializable,
        E extends AssignableMultiFile & AssignableId & AssignableCode,
        L extends LinkedFile & AssignableCode & AssignableId,
        R extends JpaPagingAndSortingRepository<I, E>,
        RL extends JpaPagingAndSortingRepository<I, L>>
        extends MultiFileServiceSubMethods<I, E, L, R, RL>
        implements IMultiFileServiceMethods<I, E> {

    @Getter
    private final Class<L> linkedFileClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[2])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<L>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine linked file class"));

    @Override
    public List<L> uploadFile(I parentId, MultipartFile[] files) throws IOException {
        Optional<E> optional = findById(parentId);
        if (optional.isPresent()) {
            E entity = optional.get();
            for (MultipartFile file : files) {
                this.uploadFile(parentId, file);
            }
            return entity.getAdditionalFiles();
        } else {
            throw new ObjectNotFoundException(getPersistentClass().getSimpleName() + " with id: " + parentId);
        }
    }

    @Override
    public List<L> uploadFile(I parentId, MultipartFile file) throws IOException {
        Optional<E> optional = findById(parentId);
        if (optional.isPresent()) {
            E entity = optional.get();
            if (file != null && !file.isEmpty()) {
                try {
                    if (file != null && !file.isEmpty()) {
                        L linkedFile = linkedFileClass.newInstance();
                        this.processCodeAssignable(linkedFile);
                        if (entity instanceof AssignableDomain assignableDomain
                                && linkedFile instanceof AssignableDomain isaasLinkedFile) {
                            isaasLinkedFile.setDomain(assignableDomain.getDomain());
                        }
                        linkedFile.setOriginalFileName(file.getOriginalFilename());
                        linkedFile.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
                        linkedFile.setPath(this.getUploadDirectory() +
                                File.separator + CrudServiceUtils.getDomainOrDefault(entity) +
                                File.separator + getPersistentClass().getSimpleName().toLowerCase() + File.separator + "additional");
                        linkedFile.setMimetype(file.getContentType());
                        linkedFile.setCrc16(CRC16Helper.calculate(file.getBytes()));
                        linkedFile.setCrc32(CRC32Helper.calculate(file.getBytes()));
                        linkedFile.setSize(file.getSize());
                        linkedFile.setVersion(1L);

                        //Uploading file
                        linkedFile = this.beforeUpload(CrudServiceUtils.getDomainOrDefault(entity),
                                linkedFile,
                                file);
                        linkedFile = subUploadFile(file, linkedFile);
                        this.afterUpload(CrudServiceUtils.getDomainOrDefault(entity),
                                linkedFile,
                                file);

                        if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
                            entity.setAdditionalFiles(new ArrayList<>());
                        }
                        entity.getAdditionalFiles().add(linkedFile);
                    } else {
                        log.warn("Upload file ({}) :File is null or empty", this.getPersistentClass().getSimpleName());
                    }
                } catch (Exception e) {
                    log.error("Update additional files failed : ", e);
                    //throw new RemoteCallFailedException(e);
                }
            }

            entity = this.update(entity);
            return entity.getAdditionalFiles();
        } else {
            throw new ObjectNotFoundException(getPersistentClass().getSimpleName() + " with id: " + parentId);
        }
    }

    @Override
    public Resource downloadFile(I parentId, I fileId, Long version) throws IOException {
        Optional<E> optional = findById(parentId);
        if (optional.isPresent()) {
            E entity = optional.get();
            L linkedFile = (L) entity.getAdditionalFiles().stream()
                    .filter(item -> ((L) item).getId().equals(fileId)).findAny()
                    .orElse(null);
            if (linkedFile != null) {
                return subDownloadFile(linkedFile, version);
            } else {
                throw new ObjectNotFoundException(this.linkedFileClass.getSimpleName() + " with id " + fileId);
            }

        } else {
            throw new ObjectNotFoundException(this.getPersistentClass().getSimpleName() + " with id " + parentId);
        }
    }

    @Override
    public boolean deleteFile(I parentId, I fileId) throws IOException {
        Optional<E> optional = findById(parentId);
        if (optional.isPresent()) {
            E entity = optional.get();
            L linkedFile = (L) entity.getAdditionalFiles().stream()
                    .filter(item -> ((L) item).getId().equals(fileId)).findAny()
                    .orElse(null);
            if (linkedFile != null) {
                entity.getAdditionalFiles().removeIf(elm -> ((L) elm).getId().equals(fileId));
                subDeleteFile(linkedFile);
                this.update(entity);
                return true;
            } else {
                throw new FileNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
            }
        } else {
            throw new ObjectNotFoundException(getPersistentClass().getSimpleName() + " with id: " + parentId);
        }
    }

    /**
     * Before upload l.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the l
     * @throws IOException the io exception
     */
    public L beforeUpload(String domain, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload l.
     *
     * @param domain the domain
     * @param entity the entity
     * @param file   the file
     * @return the l
     * @throws IOException the io exception
     */
    public L afterUpload(String domain, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();
}
