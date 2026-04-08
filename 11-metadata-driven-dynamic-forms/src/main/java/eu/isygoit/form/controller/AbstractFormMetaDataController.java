package eu.isygoit.form.controller;

import eu.isygoit.form.api.IFormMetaDataApi;
import eu.isygoit.form.domain.ViewMetaData;
import eu.isygoit.form.dto.FormSubmitRequest;
import eu.isygoit.form.dto.FormSubmitResponse;
import eu.isygoit.form.exception.MetaDataGenerationException;
import eu.isygoit.form.service.IFormMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;

import java.util.List;

/**
 * Abstract Spring MVC controller providing ready-made implementations of all
 * {@link IFormMetaDataApi} endpoints.
 * <p>
 * Subclasses must be annotated with {@code @RestController} (and optionally
 * {@code @RequestMapping} to override the base path) and inject a concrete
 * {@link IFormMetaDataService} via {@link #formMetaDataService()}.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/hr/forms")
 * public class HrFormController extends AbstractFormMetaDataController {
 *
 *     @Autowired
 *     private HrFormMetaDataService hrFormService;
 *
 *     @Override
 *     protected IFormMetaDataService formMetaDataService() {
 *         return hrFormService;
 *     }
 * }
 * }</pre>
 *
 * <h3>Hook methods</h3>
 * Override {@link #beforeGetMetaData}, {@link #afterGetMetaData}, {@link #beforeSubmit},
 * and {@link #afterSubmit} to add cross-cutting concerns (authentication checks,
 * audit logging, tenant resolution, response enrichment) without touching the
 * core metadata or submission logic.
 *
 * @see IFormMetaDataApi
 * @see IFormMetaDataService
 */
@Slf4j
public abstract class AbstractFormMetaDataController implements IFormMetaDataApi {

    // ─────────────────────────────────────────────────────────────────────────
    // Mandatory: subclass provides the service
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the {@link IFormMetaDataService} instance used by this controller.
     * <p>
     * Implement by returning the injected service bean, e.g.:
     * <pre>{@code
     *     @Autowired private MyFormService service;
     *
     *     @Override
     *     protected IFormMetaDataService formMetaDataService() { return service; }
     * }</pre>
     *
     * @return the form metadata service; must not be {@code null}
     */
    protected abstract IFormMetaDataService formMetaDataService();

    // ─────────────────────────────────────────────────────────────────────────
    // Optional hooks — override in subclasses for cross-cutting concerns
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called before metadata is resolved.
     * Override to perform auth checks, tenant validation, or parameter sanitisation.
     *
     * @param viewName the requested view name
     * @param version  the requested version
     * @param mode     the rendering mode (create / edit)
     */
    protected void beforeGetMetaData(String viewName, String version, String mode) {
        log.debug("[FormCtrl] beforeGetMetaData — view='{}', version='{}', mode='{}'", viewName, version, mode);
    }

    /**
     * Called after metadata has been resolved, before the response is returned.
     * Override to post-process or enrich the {@link ViewMetaData} (e.g. i18n label injection).
     *
     * @param viewName the requested view name
     * @param meta     the resolved metadata (may be mutated if needed)
     * @return the metadata to include in the response — return {@code meta} unchanged by default
     */
    protected ViewMetaData afterGetMetaData(String viewName, ViewMetaData meta) {
        log.debug("[FormCtrl] afterGetMetaData — view='{}', {} field(s)", viewName, meta.fields().size());
        return meta;
    }

    /**
     * Called before the submission payload is forwarded to the service.
     * Override to enrich the request (e.g. inject the current tenant into {@code fields}).
     *
     * @param viewName the target view name
     * @param request  the raw submission payload
     * @return the (possibly modified) request to pass to the service
     */
    protected FormSubmitRequest beforeSubmit(String viewName, FormSubmitRequest request) {
        log.debug("[FormCtrl] beforeSubmit — view='{}'", viewName);
        return request;
    }

    /**
     * Called after the service has produced a {@link FormSubmitResponse}.
     * Override to add audit logging, event publishing, or response transformation.
     *
     * @param viewName the submitted view name
     * @param response the response produced by the service
     * @return the (possibly modified) response to return to the client
     */
    protected FormSubmitResponse afterSubmit(String viewName, FormSubmitResponse response) {
        log.debug("[FormCtrl] afterSubmit — view='{}', success={}", viewName, response.success());
        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IFormMetaDataApi — endpoint implementations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * <p>
     * Resolves metadata from the service, calling {@link #beforeGetMetaData} and
     * {@link #afterGetMetaData} hooks around the resolution. Performance is measured
     * and logged at DEBUG level.
     */
    @Override
    public final ResponseEntity<ViewMetaData> getMetaData(String viewName,
                                                          String version,
                                                          String mode,
                                                          String entityId) {
        return executeWithTiming("getMetaData[" + viewName + "]", () -> {
            beforeGetMetaData(viewName, version, mode);

            ViewMetaData meta = "1.0".equals(version)
                    ? formMetaDataService().getMetaData(viewName)
                    : formMetaDataService().getMetaData(viewName, version);

            ViewMetaData processed = afterGetMetaData(viewName, meta);
            return ResponseEntity.ok(processed);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ResponseEntity<List<String>> listViews() {
        log.info("[FormCtrl] listViews invoked");
        return ResponseEntity.ok(formMetaDataService().getRegisteredViewNames());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The path variable {@code viewName} is injected into the request via
     * {@link #beforeSubmit} so the service always has a consistent view name,
     * regardless of what the client puts in the body.
     */
    @Override
    public final ResponseEntity<FormSubmitResponse> submit(String viewName, FormSubmitRequest request) {
        return executeWithTiming("submit[" + viewName + "]", () -> {
            // Ensure the path variable is authoritative
            FormSubmitRequest canonical = new FormSubmitRequest(viewName, request.fields());
            FormSubmitRequest enriched = beforeSubmit(viewName, canonical);

            FormSubmitResponse raw = formMetaDataService().submit(enriched);
            FormSubmitResponse response = afterSubmit(viewName, raw);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ResponseEntity<Void> invalidateCache(String viewName) {
        log.info("[FormCtrl] Cache invalidation requested for view '{}'", viewName);
        formMetaDataService().invalidateCache(viewName);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Error handling — override for custom error responses
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Translates an exception thrown during request processing into a {@link ResponseEntity}.
     * <p>
     * The default implementation maps {@link MetaDataGenerationException} and
     * {@link IllegalArgumentException} to 400, and all others to 500.
     * Override to integrate with the project's global {@code @ControllerAdvice}.
     *
     * @param e the exception to handle
     * @return an appropriate error response
     */
    protected ResponseEntity<?> handleException(Exception e) {
        log.error("[FormCtrl] Request processing failed: {}", e.getMessage(), e);
        if (e instanceof MetaDataGenerationException || e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Wraps a supplier in try/catch + StopWatch logging.
     * All endpoint methods delegate here to keep the implementation DRY.
     *
     * @param operationName label used in log messages
     * @param supplier      the operation to execute
     * @param <R>           return type
     * @return the result of the supplier, or an error response on exception
     */
    @SuppressWarnings("unchecked")
    private <R> ResponseEntity<R> executeWithTiming(String operationName,
                                                    TimedSupplier<ResponseEntity<R>> supplier) {
        StopWatch sw = new StopWatch(operationName);
        sw.start();
        try {
            ResponseEntity<R> result = supplier.get();
            sw.stop();
            log.debug("[FormCtrl] {} completed in {} ms", operationName, sw.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            if (sw.isRunning()) sw.stop();
            log.error("[FormCtrl] {} failed after {} ms", operationName, sw.getTotalTimeMillis(), e);
            return (ResponseEntity<R>) handleException(e);
        }
    }

    /**
     * Checked-exception-friendly functional interface for timed operations.
     */
    @FunctionalInterface
    private interface TimedSupplier<T> {
        T get() throws Exception;
    }
}