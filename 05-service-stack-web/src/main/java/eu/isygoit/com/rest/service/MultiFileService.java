package eu.isygoit.com.rest.service;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.exception.EmptyFileException;
import eu.isygoit.exception.EmptyFileListException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.helper.CRC16Helper;
import eu.isygoit.helper.CRC32Helper;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Multi file api.
 *
 * @param <I>  the type parameter
 * @param <T>  the type parameter
 * @param <L>  the type parameter
 * @param <R>  the type parameter
 * @param <RL> the type parameter
 */
@Slf4j
public abstract class MultiFileService<I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I> & ICodeAssignable,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I>,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>,
        RL extends JpaPagingAndSortingRepository<L, I>>
        extends MultiFileServiceSubMethods<I, T, L, R, RL>
        implements IMultiFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    private final Class<L> linkedFileClass = (Class<L>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[2];

    @Override
    public List<L> uploadAdditionalFiles(I parentId, MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new EmptyFileListException("for parent id " + parentId);
        }

        var entity = getEntityOrThrow(parentId);
        for (var file : files) {
            uploadAdditionalFile(parentId, file);
        }
        return entity.getAdditionalFiles();
    }

    @Override
    public List<L> uploadAdditionalFile(I parentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new EmptyFileException("for and parent id " + parentId);
        }

        var entity = getEntityOrThrow(parentId);
        var tenant = extractTenant(entity);

        L linkedFile;
        try {
            linkedFile = linkedFileClass.getDeclaredConstructor().newInstance();
            assignCodeIfEmpty(linkedFile);

            if (entity instanceof ITenantAssignable tenantAssignableEntity
                    && linkedFile instanceof ITenantAssignable tenantAssignableFile) {
                tenantAssignableFile.setTenant(tenantAssignableEntity.getTenant());
            }

            //linkedFile.setFileName(linkedFile.getCode() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
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
            update(entity);

        } catch (Exception e) {
            log.error("Update additional files failed: ", e);
            throw new IOException("Failed to upload additional file", e);
        }

        return entity.getAdditionalFiles();
    }

    @Override
    public ResourceDto downloadFile(I parentId, I fileId, Long version) throws IOException {
        var entity = getEntityOrThrow(parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            throw new ObjectNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
        }
        return subDownloadFile(linkedFile, version);
    }

    @Override
    public boolean deleteAdditionalFile(I parentId, I fileId) throws IOException {
        var entity = getEntityOrThrow(parentId);
        var linkedFile = findLinkedFile(entity, fileId);
        if (linkedFile == null) {
            throw new FileNotFoundException(linkedFileClass.getSimpleName() + " with id " + fileId);
        }
        entity.getAdditionalFiles().removeIf(file -> file.getId().equals(fileId));
        subDeleteFile(linkedFile);
        update(entity);
        return true;
    }

    /**
     * Gets entity or throw.
     *
     * @param id the id
     * @return the entity or throw
     */
    protected T getEntityOrThrow(I id) {
        Optional<T> optional = findById(id);
        return optional.orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + id));
    }

    /**
     * Extract tenant string.
     *
     * @param entity the entity
     * @return the string
     */
    protected String extractTenant(T entity) {
        if (entity instanceof ITenantAssignable tenantAssignable) {
            return tenantAssignable.getTenant();
        }
        return TenantConstants.DEFAULT_TENANT_NAME;
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