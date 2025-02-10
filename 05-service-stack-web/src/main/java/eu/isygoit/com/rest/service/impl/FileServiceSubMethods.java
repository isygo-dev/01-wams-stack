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

    // Cached instance of the linked file service, for efficiency
    private ILinkedFileApi linkedFileApi;

    // Abstract method to obtain an instance of the ApplicationContextService
    protected abstract ApplicationContextService getApplicationContextServiceInstance();

    // Returns an Optional containing the ApplicationContextService, if present
    protected Optional<ApplicationContextService> getApplicationContextService() {
        return Optional.ofNullable(getApplicationContextServiceInstance());
    }

    /**
     * Retrieves the linked file service based on the class annotation, or falls back
     * to using local storage if no external service is defined.
     *
     * @return the linked file service instance
     * @throws LinkedFileServiceNotDefinedException if the linked file service cannot be found
     */
    private ILinkedFileApi getLinkedFileService() throws LinkedFileServiceNotDefinedException {
        // If the linked file service is not yet fetched, attempt to retrieve it
        if (this.linkedFileApi == null) {
            // Check if the DmsLinkFileService annotation is present on the class
            var annotation = getClass().getAnnotation(DmsLinkFileService.class);

            // If annotation is found, try to obtain the bean from the application context
            if (annotation != null) {
                var applicationContextService = getApplicationContextService()
                        .orElseThrow(() -> new ApplicationContextException("ApplicationContextService not found"));

                this.linkedFileApi = applicationContextService.getBean(annotation.value()); // Fetch the bean by the class specified in the annotation
                if (Objects.isNull(this.linkedFileApi)) {
                    // Log the error and throw an exception if the bean is not found
                    String errorMessage = String.format("The linked file service bean '%s' could not be found in the application context.", annotation.value().getSimpleName());
                    log.error(errorMessage);
                    throw new LinkedFileServiceNotDefinedException(errorMessage);
                }
            } else {
                // If no annotation is found, log that local storage will be used instead
                log.warn("No linked file service defined for class '{}'. Local storage will be used as fallback.", getClass().getSimpleName());
            }
        }
        return this.linkedFileApi;
    }

    /**
     * Handles file upload for an entity, either to an external file service (if available)
     * or to local storage if no external service is defined.
     *
     * @param file   the file to upload
     * @param entity the entity to associate the uploaded file with
     * @return an Optional containing the file code if the upload is successful, or empty if it fails
     */
    final Optional<String> subUploadFile(MultipartFile file, T entity) {
        try {
            // Log the file upload initiation with entity information
            log.info("Initiating file upload for entity '{}', file: '{}'.", entity.getClass().getSimpleName(), file.getOriginalFilename());

            // Attempt to retrieve the linked file service
            ILinkedFileApi linkedFileService = getLinkedFileService();

            // If a linked file service is available, upload the file to the external service
            if (linkedFileService != null) {
                log.info("Uploading file to external file service.");
                return FileServiceDmsStaticMethods.upload(file, entity, linkedFileService)
                        .map(response -> {
                            // Log a success message and return the file code upon successful upload
                            log.info("File uploaded to external service successfully with code '{}'.", response.getCode());
                            return response.getCode();
                        });
            } else {
                // If no external service is available, fall back to uploading the file locally
                log.info("No external file service found, uploading file to local storage.");
                String fileCode = FileServiceLocalStaticMethods.upload(file, entity);
                log.info("File uploaded locally with code '{}'.", fileCode);
                return Optional.of(fileCode);
            }
        } catch (Exception e) {
            // Log the error message in case of an exception during the upload process
            String errorMessage = String.format("File upload failed for entity '%s'. File: '%s'", entity.getClass().getSimpleName(), file.getOriginalFilename());
            log.error(errorMessage, e);
        }
        return Optional.empty();  // Return empty Optional if upload fails
    }

    /**
     * Handles file download for an entity, either from an external file service (if available)
     * or from local storage if no external service is defined.
     *
     * @param entity  the entity associated with the file
     * @param version the version of the file to download
     * @return the Resource representing the downloaded file, or null if the download fails
     */
    final Resource subDownloadFile(T entity, Long version) {
        try {
            // Log the file download initiation with entity and version information
            log.info("Initiating download for entity '{}', version: '{}'.", entity.getClass().getSimpleName(), version);

            // Attempt to retrieve the linked file service
            ILinkedFileApi linkedFileService = getLinkedFileService();

            // If a linked file service is available, attempt to download the file from it
            if (linkedFileService != null) {
                log.info("Downloading file from external file service.");
                return FileServiceDmsStaticMethods.download(entity, version, linkedFileService);
            } else {
                // If no external service is available, fall back to downloading the file locally
                log.info("No external file service found, downloading file from local storage.");
                return FileServiceLocalStaticMethods.download(entity, version);
            }
        } catch (Exception e) {
            // Log an error message if the download process fails
            String errorMessage = String.format("File download failed for entity '%s', version '%d'.", entity.getClass().getSimpleName(), version);
            log.error(errorMessage, e);
        }
        return null;  // Return null if download fails (you could consider throwing an exception instead)
    }
}