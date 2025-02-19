package eu.isygoit.com.rest.service.impl.file.sub;

import eu.isygoit.com.rest.api.IDmsLinkedFileService;
import eu.isygoit.com.rest.service.impl.utils.DmsFileStorageUtils;
import eu.isygoit.com.rest.service.impl.utils.LocalFileStorageUtils;
import eu.isygoit.com.rest.service.impl.utils.MultiFileServiceUtils;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.AssignableMultiFile;
import eu.isygoit.model.LinkedFile;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type Multi file service sub methods.
 *
 * @param <I>  the type parameter
 * @param <E>  the type parameter
 * @param <L>  the type parameter
 * @param <R>  the type parameter
 * @param <RL> the type parameter
 */
@Slf4j
public abstract class MultiFileServiceSubMethods<E extends AssignableMultiFile & AssignableId & AssignableCode,
        I extends Serializable,
        L extends LinkedFile & AssignableCode & AssignableId,
        R extends JpaPagingAndSortingRepository<E, I>,
        RL extends JpaPagingAndSortingRepository<L, I>>
        extends MultiFileServiceUtils<E, I, L, R, RL> {

    /**
     * Sub upload file l.
     *
     * @param file   the file
     * @param entity the entity
     * @return the l
     */
    public final L subUploadFile(MultipartFile file, L entity) {
        try {
            IDmsLinkedFileService linkedFileService = this.getDmsLinkedFileService();
            if (linkedFileService != null) {
                entity.setCode(DmsFileStorageUtils.uploadFile(file, entity, linkedFileService).getCode());
            } else {
                entity.setCode(LocalFileStorageUtils.uploadFile(file, entity));
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }

        return entity;
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    public final Resource subDownloadFile(L entity, Long version) {
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

    /**
     * Sub delete file boolean.
     *
     * @param entity the entity
     * @return the boolean
     */
    public final boolean subDeleteFile(L entity) {
        try {
            linkedFileRepository().delete(entity);
            IDmsLinkedFileService linkedFileService = this.getDmsLinkedFileService();
            if (linkedFileService != null) {
                return DmsFileStorageUtils.deleteFile(entity, linkedFileService);
            } else {
                return LocalFileStorageUtils.deleteFile(entity);
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }

        return false;
    }
}
