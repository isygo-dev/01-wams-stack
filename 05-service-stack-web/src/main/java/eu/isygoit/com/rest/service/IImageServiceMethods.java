package eu.isygoit.com.rest.service;

import eu.isygoit.model.AssignableId;
import eu.isygoit.model.AssignableImage;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface Image service methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface IImageServiceMethods<I extends Serializable, E extends AssignableId & AssignableImage> {

    /**
     * Upload image e.
     *
     * @param senderDomain the sender domain
     * @param id           the id
     * @param image        the image
     * @return the e
     * @throws IOException the io exception
     */
    E uploadImage(String senderDomain, I id, MultipartFile image) throws IOException;

    /**
     * Download image resource.
     *
     * @param id the id
     * @return the resource
     * @throws IOException the io exception
     */
    Resource downloadImage(I id) throws IOException;

    /**
     * Create with image e.
     *
     * @param senderDomain the sender domain
     * @param entity       the entity
     * @param file         the file
     * @return the e
     * @throws IOException the io exception
     */
    E createWithImage(String senderDomain, E entity, MultipartFile file) throws IOException;

    /**
     * Update with image e.
     *
     * @param senderDomain the sender domain
     * @param entity       the entity
     * @param file         the file
     * @return the e
     * @throws IOException the io exception
     */
    E updateWithImage(String senderDomain, E entity, MultipartFile file) throws IOException;
}
