package eu.isygoit.com.rest.service;

import eu.isygoit.constants.DomainConstants;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.helper.CRC16Helper;
import eu.isygoit.helper.CRC32Helper;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
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

@Slf4j
public abstract class MultiFileService<I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I>,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I>,
        R extends JpaPagingAndSortingRepository<T, I>,
        RL extends JpaPagingAndSortingRepository<L, I>>
        extends MultiFileServiceSubMethods<I, T, L, R, RL>
        implements IMultiFileServiceMethods<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    private final Class<L> linkedFileClass = (Class<L>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[2];

    @Override
    public List<L> uploadAdditionalFiles(I parentId, MultipartFile[] files) throws IOException {
        var entity = getEntityOrThrow(parentId);
        for (var file : files) {
            uploadAdditionalFile(parentId, file);
        }
        return entity.getAdditionalFiles();
    }

    @Override
    public List<L> uploadAdditionalFile(I parentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Upload file ({}): file is null or empty", persistentClass.getSimpleName());
            return getEntityOrThrow(parentId).getAdditionalFiles();
        }

        var entity = getEntityOrThrow(parentId);
        var domain = extractDomain(entity);

        L linkedFile;
        try {
            linkedFile = linkedFileClass.getDeclaredConstructor().newInstance();
            assignCodeIfEmpty(linkedFile);

            if (entity instanceof IDomainAssignable domainAssignableEntity
                    && linkedFile instanceof IDomainAssignable domainAssignableFile) {
                domainAssignableFile.setDomain(domainAssignableEntity.getDomain());
            }

            var originalFilename = file.getOriginalFilename();
            linkedFile.setOriginalFileName(originalFilename);
            linkedFile.setExtension(FilenameUtils.getExtension(originalFilename));
            linkedFile.setPath(Path.of(getUploadDirectory())
                    .resolve(domain)
                    .resolve(persistentClass.getSimpleName().toLowerCase())
                    .resolve("additional").toString());
            linkedFile.setMimetype(file.getContentType());
            var bytes = file.getBytes();
            linkedFile.setCrc16(CRC16Helper.calculate(bytes));
            linkedFile.setCrc32(CRC32Helper.calculate(bytes));
            linkedFile.setSize(file.getSize());
            linkedFile.setVersion(1L);

            linkedFile = beforeUpload(domain, linkedFile, file);
            linkedFile = subUploadFile(file, linkedFile);
            linkedFile = afterUpload(domain, linkedFile, file);

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
    public Resource downloadFile(I parentId, I fileId, Long version) throws IOException {
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

    protected T getEntityOrThrow(I id) {
        Optional<T> optional = findById(id);
        return optional.orElseThrow(() ->
                new ObjectNotFoundException(persistentClass.getSimpleName() + " with id: " + id));
    }

    protected String extractDomain(T entity) {
        if (entity instanceof IDomainAssignable domainAssignable) {
            return domainAssignable.getDomain();
        }
        return DomainConstants.DEFAULT_DOMAIN_NAME;
    }

    protected L findLinkedFile(T entity, I fileId) {
        if (entity.getAdditionalFiles() == null) return null;
        return entity.getAdditionalFiles().stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElse(null);
    }

    public L beforeUpload(String domain, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    public L afterUpload(String domain, L entity, MultipartFile file) throws IOException {
        return entity;
    }

    protected abstract String getUploadDirectory();
}