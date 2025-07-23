package eu.isygoit.com.rest.controller.impl.tenancy;

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
public interface IImageTenantServiceMethods<I extends Serializable, T extends IIdAssignable<I> & IImageEntity> {

    /**
     * Upload image t.
     *
     * @param tenant the sender tenant
     * @param id     the id
     * @param image  the image
     * @return the t
     * @throws IOException the io exception
     */
    T uploadImage(String tenant, I id, MultipartFile image) throws IOException;

    /**
     * Download image resource.
     *
     * @param tenant the tenant
     * @param id     the id
     * @return the resource
     * @throws IOException the io exception
     */
    ResourceDto downloadImage(String tenant, I id) throws IOException;

    /**
     * Create with image t.
     *
     * @param tenant the sender tenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    T createWithImage(String tenant, T entity, MultipartFile file) throws IOException;

    /**
     * Update with image t.
     *
     * @param tenant the sender tenant
     * @param entity the entity
     * @param file   the file
     * @return the t
     * @throws IOException the io exception
     */
    T updateWithImage(String tenant, T entity, MultipartFile file) throws IOException;
}
