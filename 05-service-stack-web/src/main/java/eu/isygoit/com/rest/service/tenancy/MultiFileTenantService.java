package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.helper.CRC16Helper;
import eu.isygoit.helper.CRC32Helper;
import eu.isygoit.model.*;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Multi file service.
 *
 * @param <I>  the type parameter
 * @param <T>  the type parameter
 * @param <L>  the type parameter
 * @param <R>  the type parameter
 * @param <RL> the type parameter
 */
@Slf4j
public abstract class MultiFileTenantService<I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I> & ITenantAssignable,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>,
        RL extends JpaPagingAndSortingTenantAssignableRepository<L, I>>
        extends MultiFileTenantServiceSubMethods<I, T, L, R, RL>
        implements IMultiFileTenantServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    private final Class<L> linkedFileClass = (Class<L>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[2];

    @Override
    public List<L> uploadAdditionalFiles(String tenant, I parentId, MultipartFile[] files) throws IOException {
        var entity = getEntityOrThrow(tenant, parentId);
        for (var file : files) {
            uploadAdditionalFile(tenant, parentId, file);
        }
        return entity.getAdditionalFiles();
    }

    @Override
    public List<L> uploadAdditionalFile(String tenant, I parentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Upload file ({}): file is null or empty", persistentClass.getSimpleName());
            return getEntityOrThrow(tenant, parentId).getAdditionalFiles();
        }

        var entity = getEntityOrThrow(tenant, parentId);

        L linkedFile;
        try {
            linkedFile = linkedFileClass.getDeclaredConstructor().newInstance();
            assignCodeIfEmpty(linkedFile);
            linkedFile.setTenant(tenant);
            var originalFilename = file.getOriginalFilename();
            linkedFile.setOriginalFileName(originalFilename);
            linkedFile.setExtension(FilenameUtils.getExtension(originalFilename));
            linkedFile.setPath(Path.of(getUploadDirectory())
                    .resolve(tenant)
                    .resolve(persistentClass.getSimpleName().toLowerCase())
                    .resolve("additional").toString());
            linkedFile.setMimetype(file.getContentType());
            var bytes = file.getBytes();
            linkedFile.setCrc16(CRC16Helper.calculate(bytes));
            linkedFile.setCrc32(CRC32Helper.calculate(bytes));
            linkedFile.setSize(file.getSize());
            linkedFile.setVersion(1L);

            linkedFile = beforeUpload(tenant, linkedFile, file);
            linkedFile = subUploadFile(file, linkedFile);
            linkedFile = afterUpload(tenant, linkedFile, file);

            if (CollectionUtils.isEmpty(entity.getAdditionalFiles())) {
                entity.setAdditionalFiles(new ArrayList<>());
            }
            entity.getAdditionalFiles().add(linkedFile);
            update(tenant, entity);
        } catch (Exception e) {
            log.error("Update additional files failed: ", e);
            throw new IOException("Failed to upload additional file", e);
        }

        return entity.getAdditionalFiles();
    }

    @Override
    public Resource downloadFile(String tenant, I parentId, I fileId, Long version) throws IOException {
        var entity = getEntityOrThrow(tenant, parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            throw new ObjectNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
        }
        return subDownloadFile(linkedFile, version);
    }

    @Override
    public boolean deleteAdditionalFile(String tenant, I parentId, I fileId) throws IOException {
        var entity = getEntityOrThrow(tenant, parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            throw new FileNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
        }
        entity.getAdditionalFiles().removeIf(file -> file.getId().equals(fileId));
        subDeleteFile(linkedFile);
        update(tenant, entity);
        return true;
    }

    /**
     * Gets entity or throw.
     *
     * @param tenant the tenant
     * @param id     the id
     * @return the entity or throw
     */
    protected T getEntityOrThrow(String tenant, I id) {
        Optional<T> optional = findById(tenant, id);
        return optional.orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + id));
    }

    /**
     * Find linked file l.
     *
     * @param entity the entity
     * @param fileId the file id
     * @return the l
     */
    protected L findLinkedFile(T entity, I fileId) {
        if (entity.getAdditionalFiles() == null) return null;
        return entity.getAdditionalFiles().stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Before upload l.
     *
     * @param tenant the tenant
     * @param entity the entity
     * @param file   the file
     * @return the l
     * @throws IOException the io exception
     */
    public L beforeUpload(String tenant, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * After upload l.
     *
     * @param tenant the tenant
     * @param entity the entity
     * @param file   the file
     * @return the l
     * @throws IOException the io exception
     */
    public L afterUpload(String tenant, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    /**
     * Gets upload directory.
     *
     * @return the upload directory
     */
    protected abstract String getUploadDirectory();
}