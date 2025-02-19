package eu.isygoit.com.rest.service;

import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface File service methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface IFileServiceMethods<E extends AssignableId & AssignableFile,
        I extends Serializable> {

    /**
     * Create with file e.
     *
     * @param senderDomain the sender domain
     * @param entity       the entity
     * @param file         the file
     * @return the e
     * @throws IOException the io exception
     */
    @Transactional
    E createWithFile(String senderDomain, E entity, MultipartFile file) throws IOException;

    /**
     * Update with file e.
     *
     * @param senderDomain the sender domain
     * @param id           the id
     * @param entity       the entity
     * @param file         the file
     * @return the e
     * @throws IOException the io exception
     */
    @Transactional
    E updateWithFile(String senderDomain, I id, E entity, MultipartFile file) throws IOException;

    /**
     * Upload file e.
     *
     * @param senderDomain the sender domain
     * @param id           the id
     * @param file         the file
     * @return the e
     * @throws IOException the io exception
     */
    @Transactional
    E uploadFile(String senderDomain, I id, MultipartFile file) throws IOException;

    /**
     * Download file resource.
     *
     * @param id      the id
     * @param version the version
     * @return the resource
     * @throws IOException the io exception
     */
    Resource downloadFile(I id, Long version) throws IOException;
}
