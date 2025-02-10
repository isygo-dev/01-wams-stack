package eu.isygoit;

import eu.isygoit.api.IApiExtractor;
import eu.isygoit.app.ApplicationContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Abstract base class for service starters, responsible for extracting API metadata
 * from Spring-managed controllers after application startup.
 * <p>
 * This class scans the application context for beans annotated with {@link RestController},
 * and uses an API extractor to analyze their exposed endpoints.
 * <p>
 * Inherited services should autowire:
 * private ApplicationContextService applicationContextService;
 * private IApiExtractor apiExtractor;
 * </p>
 */
@Slf4j
public abstract class ServiceStarter {

    /**
     * Log message template for API extraction failure.
     */
    public static final String ERROR_EXTRACT_API_FAILS = "<Error>: Failed to extract API for {}";

    /**
     * Provides an optional application context service, which allows retrieving beans annotated with
     * {@link RestController}.
     *
     * @return an optional application context service
     */
    protected Optional<ApplicationContextService> getApplicationContextService() {
        return Optional.ofNullable(getApplicationContextServiceInstance());
    }

    /**
     * Gets application context service instance.
     *
     * @return the application context service instance
     */
    protected abstract ApplicationContextService getApplicationContextServiceInstance();

    /**
     * Provides an optional API extractor responsible for processing controllers.
     *
     * @return an optional API extractor, which may be empty if no extractor is available
     */
    protected Optional<IApiExtractor> getApiExtractor() {
        return Optional.ofNullable(getApiExtractorInstance());
    }

    /**
     * Gets api extractor instance.
     *
     * @return the api extractor instance
     */
    protected abstract IApiExtractor getApiExtractorInstance();


    /**
     * Extracts API metadata from all registered {@link RestController} beans once the application
     * is fully initialized.
     * <p>
     * This method is triggered automatically by the {@link ApplicationReadyEvent} and iterates
     * over all controllers, attempting to extract their API details.
     * </p>
     * <p>
     * If the application context or API extractor is missing, appropriate warnings are logged.
     * If an exception occurs during extraction, it is logged as an error.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public final void extractApis() {
        // Ensure that ApplicationContextService is available before proceeding
        getApplicationContextService().ifPresentOrElse(
                appContextService -> {
                    appContextService.getBeansWithAnnotation(RestController.class)
                            .values()
                            .forEach(controller -> {
                                String controllerName = controller.getClass().getSimpleName();
                                // Extract API details if API extractor is present
                                getApiExtractor().ifPresentOrElse(
                                        extractor -> {
                                            try {
                                                extractor.extractApis(controller.getClass());
                                            } catch (Exception e) {
                                                // Log generic extraction failures
                                                log.error(ERROR_EXTRACT_API_FAILS, controllerName, e);
                                            }
                                        },
                                        () -> log.warn("<Warning>: API Extractor is null for {}", controllerName)
                                );
                            });
                },
                () -> log.warn("<Warning>: ApplicationContextService is not available, skipping API extraction")
        );
    }
}