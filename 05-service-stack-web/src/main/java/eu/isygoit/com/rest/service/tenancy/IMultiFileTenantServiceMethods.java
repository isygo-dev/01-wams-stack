package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IMultiFileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * The interface Multi file api methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface IMultiFileTenantServiceMethods<I extends Serializable, T extends IIdAssignable<I> & IMultiFileEntity> {

    /**
     * Upload additional files list.
     *
     * @param tenant   the tenant
     * @param parentId the parent id
     * @param files    the files
     * @return the list
     * @throws IOException the io exception
     */
    List uploadAdditionalFiles(String tenant, I parentId, MultipartFile[] files) throws IOException;

    /**
     * Upload additional file list.
     *
     * @param tenant   the tenant
     * @param parentId the parent id
     * @param file     the file
     * @return the list
     * @throws IOException the io exception
     */
    List uploadAdditionalFile(String tenant, I parentId, MultipartFile file) throws IOException;

    /**
     * Download file resource.
     *
     * @param tenant   the tenant
     * @param parentId the parent id
     * @param fileId   the file id
     * @param version  the version
     * @return the resource
     * @throws IOException the io exception
     */
    Resource downloadFile(String tenant, I parentId, I fileId, Long version) throws IOException;

    /**
     * Delete additional file boolean.
     *
     * @param tenant   the tenant
     * @param parentId the parent id
     * @param fileId   the file id
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean deleteAdditionalFile(String tenant, I parentId, I fileId) throws IOException;
}
