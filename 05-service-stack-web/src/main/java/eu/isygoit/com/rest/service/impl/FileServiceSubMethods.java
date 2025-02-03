package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.dto.common.LinkedFileResponseDto;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

/**
 * The type File service sub methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileServiceSubMethods<I, T extends IFileEntity & IIdEntity & ICodifiable, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (Objects.isNull(this.linkedFileApi)) {
            DmsLinkFileService annotation = this.getClass().getAnnotation(DmsLinkFileService.class);
            if (Objects.nonNull(annotation)) {
                this.linkedFileApi = applicationContextService.getBean(annotation.value());
                if (Objects.isNull(this.linkedFileApi)) {
                    log.error("<Error>: bean {} not found", annotation.value().getSimpleName());
                    throw new LinkedFileServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found");
                }
            } else {
                log.error("<Error>: Linked file service not defined for {}, local storage will be used!", this.getClass().getSimpleName());
            }
        }

        return this.linkedFileApi;
    }

    /**
     * Sub upload file optional.
     *
     * @param file   the file
     * @param entity the entity
     * @return the optional
     */
    final Optional<String> subUploadFile(MultipartFile file, T entity) {
        try {
            ILinkedFileApi linkedFileService = this.linkedFileService();
            if (Objects.nonNull(linkedFileService)) {
                Optional<LinkedFileResponseDto> optional = FileServiceDmsStaticMethods.upload(file, entity, linkedFileService);
                if (optional.isPresent()) {
                    log.info("File uploaded successfully with code {}", optional.get().getCode());
                    return Optional.ofNullable(optional.get().getCode());
                }
            } else {
                String fileCode = FileServiceLocalStaticMethods.upload(file, entity);
                log.info("File uploaded locally with code {}", fileCode);
                return Optional.ofNullable(fileCode);
            }
        } catch (Exception e) {
            log.error("File upload failed: ", e);
        }

        return Optional.empty();
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    final Resource subDownloadFile(T entity, Long version) {
        try {
            ILinkedFileApi linkedFileService = this.linkedFileService();
            if (Objects.nonNull(linkedFileService)) {
                return FileServiceDmsStaticMethods.download(entity, version, linkedFileService);
            } else {
                return FileServiceLocalStaticMethods.download(entity, version);
            }
        } catch (Exception e) {
            log.error("File download failed: ", e);
        }
        return null;  // Consider throwing an exception if downloading fails
    }
}