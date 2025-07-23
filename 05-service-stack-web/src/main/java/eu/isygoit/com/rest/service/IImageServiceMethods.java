package eu.isygoit.com.rest.service;

import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface Image api methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface IImageServiceMethods<I extends Serializable, T extends IIdAssignable<I> & IImageEntity> {

    /**
     * Upload image t.
     *
     * @param id    the id
     * @param image the image
     * @return the t
     * @throws IOException the io exception
     */
    T uploadImage(I id, MultipartFile image) throws IOException;

    /**
     * Download image resource.
     *
     * @param id the id
     * @return the resource
     * @throws IOException the io exception
     */
    ResourceDto downloadImage(I id) throws IOException;

    /**
     * Create with image t.
     *
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    T createWithImage(T entity, MultipartFile file) throws IOException;

    /**
     * Update with image t.
     *
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    T updateWithImage(T entity, MultipartFile file) throws IOException;
}
