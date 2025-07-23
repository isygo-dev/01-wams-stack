package eu.isygoit.com.rest.service;

import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface File api methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface IFileServiceMethods<I extends Serializable,
        T extends IIdAssignable<I> & IFileEntity> {

    /**
     * Create with file t.
     *
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    @Transactional
    T createWithFile(T entity, MultipartFile file) throws IOException;

    /**
     * Update with file t.
     *
     * @param id     the id
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    @Transactional
    T updateWithFile(I id, T entity, MultipartFile file) throws IOException;

    /**
     * Upload file t.
     *
     * @param id   the id
     * @param file the file
     * @return the t
     * @throws IOException the io exception
     */
    @Transactional
    T uploadFile(I id, MultipartFile file) throws IOException;

    /**
     * Download file resource.
     *
     * @param id      the id
     * @param version the version
     * @return the resource
     * @throws IOException the io exception
     */
    ResourceDto downloadFile(I id, Long version) throws IOException;
}
