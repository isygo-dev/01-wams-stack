package eu.isygoit.com.rest.service;

import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.IMultiFileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * The interface Multi file service methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface IMultiFileServiceMethods<I extends Serializable, E extends IIdEntity & IMultiFileEntity> {

    /**
     * Upload additional files list.
     *
     * @param parentId the parent id
     * @param files    the files
     * @return the list
     * @throws IOException the io exception
     */
    List uploadAdditionalFiles(I parentId, MultipartFile[] files) throws IOException;

    /**
     * Upload additional file list.
     *
     * @param parentId the parent id
     * @param file     the file
     * @return the list
     * @throws IOException the io exception
     */
    List uploadAdditionalFile(I parentId, MultipartFile file) throws IOException;

    /**
     * Download file resource.
     *
     * @param parentId the parent id
     * @param fileId   the file id
     * @param version  the version
     * @return the resource
     * @throws IOException the io exception
     */
    Resource downloadFile(I parentId, I fileId, Long version) throws IOException;

    /**
     * Delete additional file boolean.
     *
     * @param parentId the parent id
     * @param fileId   the file id
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean deleteAdditionalFile(I parentId, I fileId) throws IOException;
}
