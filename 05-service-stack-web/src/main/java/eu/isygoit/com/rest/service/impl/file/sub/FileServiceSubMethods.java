package eu.isygoit.com.rest.service.impl.file.sub;

import eu.isygoit.com.rest.api.IDmsLinkedFileService;
import eu.isygoit.com.rest.service.impl.utils.DmsFileStorageUtils;
import eu.isygoit.com.rest.service.impl.utils.FileServiceUtils;
import eu.isygoit.com.rest.service.impl.utils.LocalFileStorageUtils;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type File service sub methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileServiceSubMethods<E extends AssignableFile & AssignableId & AssignableCode,
        I extends Serializable,
        R extends JpaPagingAndSortingRepository<E, I>>
        extends FileServiceUtils<E, I, R> {

    /**
     * Sub upload file string.
     *
     * @param file   the file
     * @param entity the entity
     * @return the string
     */
    public final String subUploadFile(MultipartFile file, E entity) {
        try {
            IDmsLinkedFileService linkedFileService = this.getDmsLinkedFileService();
            if (linkedFileService != null) {
                return DmsFileStorageUtils.uploadFile(file, entity, linkedFileService).getCode();
            } else {
                return LocalFileStorageUtils.uploadFile(file, entity);
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }

        return null;
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    public final Resource subDownloadFile(E entity, Long version) {
        try {
            IDmsLinkedFileService linkedFileService = this.getDmsLinkedFileService();
            if (linkedFileService != null) {
                return DmsFileStorageUtils.downloadFile(entity, version, linkedFileService);
            } else {
                return LocalFileStorageUtils.downloadFile(entity, version);
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }
        return null;
    }
}
