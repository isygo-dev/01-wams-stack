package eu.isygoit.form.api;

import eu.isygoit.form.domain.ViewMetaData;
import eu.isygoit.form.dto.FormSubmitRequest;
import eu.isygoit.form.dto.FormSubmitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API contract for dynamic form metadata endpoints.
 * <p>
 * Provides three endpoint groups:
 * <ul>
 *   <li>{@code GET  /api/v1/forms/{viewName}/metadata}  — retrieve view metadata</li>
 *   <li>{@code GET  /api/v1/forms}                       — list all registered view names</li>
 *   <li>{@code POST /api/v1/forms/{viewName}/submit}     — submit and validate form data</li>
 * </ul>
 *
 * @see eu.isygoit.form.controller.AbstractFormMetaDataController
 */
@Tag(name = "Form Metadata", description = "Dynamic form metadata and submission API")
@RequestMapping(value = "/api/v1/forms", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IFormMetaDataApi {

    /**
     * Returns the full metadata descriptor for a registered form view.
     *
     * @param viewName the logical view name declared in {@code @FormView#name()}
     * @param version  optional version string; defaults to "1.0" when omitted
     * @param mode     optional rendering mode: {@code create} (default) or {@code edit}
     * @param entityId optional entity ID used in {@code edit} mode to pre-fill defaults
     * @return {@link ViewMetaData} describing fields, validations, options, and UI config
     */
    @Operation(
            summary = "Get form metadata",
            description = "Returns the complete metadata descriptor for a registered view, " +
                    "including all field types, validation rules, options, and conditional logic.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Metadata resolved successfully",
                            content = @Content(schema = @Schema(implementation = ViewMetaData.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid view name or version"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
                    @ApiResponse(responseCode = "404", description = "View not registered"),
                    @ApiResponse(responseCode = "500", description = "Metadata generation failed")
            }
    )
    @GetMapping("/{viewName}/metadata")
    ResponseEntity<ViewMetaData> getMetaData(
            @Parameter(description = "Logical view name", required = true)
            @PathVariable String viewName,

            @Parameter(description = "View version (default: 1.0)")
            @RequestParam(required = false, defaultValue = "1.0") String version,

            @Parameter(description = "Rendering mode: create | edit")
            @RequestParam(required = false, defaultValue = "create") String mode,

            @Parameter(description = "Entity ID for pre-filling fields in edit mode")
            @RequestParam(required = false) String entityId
    );

    /**
     * Returns the names of all currently registered form views.
     *
     * @return list of view names
     */
    @Operation(
            summary = "List registered views",
            description = "Returns the names of all form views registered in this service.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "View names returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping
    ResponseEntity<List<String>> listViews();

    /**
     * Validates and processes a form submission.
     * <p>
     * Applies annotation-driven rules from the view metadata before delegating to the
     * service's {@code doSubmit} implementation.
     *
     * @param viewName the view being submitted
     * @param request  the submission payload containing field values
     * @return {@link FormSubmitResponse} with success flag and any field-level errors
     */
    @Operation(
            summary = "Submit form",
            description = "Validates the submitted fields against the view's annotation-driven rules " +
                    "and processes the submission. Returns field-level error details on failure.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Form submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation errors — see fieldErrors in response body"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
                    @ApiResponse(responseCode = "404", description = "View not registered"),
                    @ApiResponse(responseCode = "500", description = "Server error during submission")
            }
    )
    @PostMapping(value = "/{viewName}/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FormSubmitResponse> submit(
            @Parameter(description = "Logical view name", required = true)
            @PathVariable String viewName,

            @RequestBody FormSubmitRequest request
    );

    /**
     * Evicts cached metadata for the given view (admin / dev operation).
     *
     * @param viewName the view whose cache should be evicted
     * @return 204 No Content on success
     */
    @Operation(
            summary = "Invalidate metadata cache",
            description = "Forces re-generation of metadata on next access for the given view.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cache evicted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
                    @ApiResponse(responseCode = "404", description = "View not registered"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @DeleteMapping("/{viewName}/cache")
    ResponseEntity<Void> invalidateCache(
            @Parameter(description = "Logical view name", required = true)
            @PathVariable String viewName
    );
}