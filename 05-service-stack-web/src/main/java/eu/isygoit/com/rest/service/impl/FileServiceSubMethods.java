package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

/**
 * Abstract service class that provides methods for uploading and downloading files
 * associated with entities, with support for both external (DMS) and local storage.
 *
 * @param <I> the type of the ID
 * @param <T> the type of the entity, which must extend IFileEntity, IIdEntity, and ICodifiable
 * @param <R> the type of the repository for the entity
 */
@Slf4j
public abstract class FileServiceSubMethods<I, T extends IFileEntity & IIdEntity & ICodifiable, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R> {

    private ILinkedFileApi linkedFileApi;

    // Abstract method to obtain an instance of the ApplicationContextService
    protected abstract ApplicationContextService getApplicationContextServiceInstance();

    protected Optional<ApplicationContextService> getApplicationContextService() {
        return Optional.ofNullable(getApplicationContextServiceInstance());
    }

    // Method to get the linked file service from the context, falls back to null if not found
    private ILinkedFileApi getLinkedFileService() throws LinkedFileServiceNotDefinedException {
        if (this.linkedFileApi == null) {
            var annotation = getClass().getAnnotation(DmsLinkFileService.class);

            // If annotation is present, fetch the linked file service bean from the context
            if (annotation != null) {
                var applicationContextService = getApplicationContextService()
                        .orElseThrow(() -> new ApplicationContextException("ApplicationContextService not found"));

                this.linkedFileApi = applicationContextService.getBean(annotation.value());
                if (Objects.isNull(this.linkedFileApi)) {
                    String errorMessage = String.format("The linked file service bean '%s' could not be found.", annotation.value().getSimpleName());
                    log.error(errorMessage);
                    throw new LinkedFileServiceNotDefinedException(errorMessage);
                }
            } else {
                log.warn("No linked file service defined for class '{}'. Local storage will be used.", getClass().getSimpleName());
            }
        }
        return this.linkedFileApi;
    }

    // Functional Interface for file upload
    @FunctionalInterface
    interface FileUploadAction<T> {
        Optional<String> upload(MultipartFile file, T entity) throws Exception;
    }

    // Functional Interface for file download
    @FunctionalInterface
    interface FileDownloadAction<T> {
        Resource download(T entity, Long version) throws Exception;
    }

    /**
     * Handles file upload, using either a linked file service or local storage.
     * Logs details about the upload process for traceability.
     *
     * @param file   the file to upload
     * @param entity the entity to associate with the uploaded file
     * @return an Optional containing the file code if the upload is successful, or empty if it fails
     */
    final Optional<String> subUploadFile(MultipartFile file, T entity) {
        try {
            log.info("Starting file upload for entity '{}', file: '{}'.", entity.getClass().getSimpleName(), file.getOriginalFilename());

            // Get the linked file service if available
            ILinkedFileApi linkedFileService = getLinkedFileService();
            FileUploadAction<T> uploadAction;

            // If a linked file service exists, upload the file to it
            if (linkedFileService != null) {
                log.info("Uploading file to external file service.");
                uploadAction = (f, e) -> FileServiceDmsStaticMethods.upload(f, e, linkedFileService)
                        .map(response -> {
                            log.info("File successfully uploaded to external service with code '{}'.", response.getCode());
                            return response.getCode();
                        });
            } else {
                // If no external service is available, upload the file locally
                log.info("No external file service found, uploading file to local storage.");
                uploadAction = (f, e) -> {
                    String fileCode = FileServiceLocalStaticMethods.upload(f, e);
                    log.info("File successfully uploaded locally with code '{}'.", fileCode);
                    return Optional.of(fileCode);
                };
            }

            // Execute the upload action
            return uploadAction.upload(file, entity);
        } catch (Exception e) {
            log.error("File upload failed for entity '{}'. File: '{}'. Error: {}", entity.getClass().getSimpleName(), file.getOriginalFilename(), e.getMessage(), e);
        }
        return Optional.empty();  // Return an empty Optional if upload fails
    }

    /**
     * Handles file download, using either a linked file service or local storage.
     * Logs details about the download process for traceability.
     *
     * @param entity  the entity associated with the file
     * @param version the version of the file to download
     * @return the Resource representing the downloaded file, or null if the download fails
     */
    final Resource subDownloadFile(T entity, Long version) {
        try {
            log.info("Starting file download for entity '{}', version: '{}'.", entity.getClass().getSimpleName(), version);

            // Get the linked file service if available
            ILinkedFileApi linkedFileService = getLinkedFileService();
            FileDownloadAction<T> downloadAction;

            // If a linked file service exists, download the file from it
            if (linkedFileService != null) {
                log.info("Downloading file from external file service.");
                downloadAction = (e, v) -> FileServiceDmsStaticMethods.download(e, v, linkedFileService);
            } else {
                // If no external service is available, download the file locally
                log.info("No external file service found, downloading file from local storage.");
                downloadAction = (e, v) -> FileServiceLocalStaticMethods.download(e, v);
            }

            // Execute the download action
            return downloadAction.download(entity, version);
        } catch (Exception e) {
            log.error("File download failed for entity '{}', version '{}'. Error: {}", entity.getClass().getSimpleName(), version, e.getMessage(), e);
        }
        return null;  // Return null if download fails
    }
}