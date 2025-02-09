package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Generic API for fetching resources with various criteria.
 *
 * @param <I>       Type of the resource identifier (must be serializable).
 * @param <MinDto>  DTO for minimal data representation.
 * @param <FullDto> DTO for full data representation.
 */
public interface IMappedCrudFetchApi<I extends Serializable, MinDto extends IIdentifiableDto, FullDto extends MinDto> {

    /**
     * Retrieves all resources with minimal data.
     *
     * @param requestContext Optional user context.
     * @return List of resources.
     */
    @Operation(summary = "Fetch all resources (minimal data)",
            description = "Retrieves a list of resources with minimal data representation.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved resources")
    @GetMapping
    ResponseEntity<List<MinDto>> getAll(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Retrieves all resources with minimal data associated with the default domain.
     *
     * @param requestContext Optional user context.
     * @return List of resources.
     */
    @Operation(summary = "Fetch all resources (minimal data for default domain)",
            description = "Fetches all resources associated with the default domain.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved resources")
    @GetMapping("/default-domain")
    ResponseEntity<List<MinDto>> getAssignedToDefaultDomain(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Retrieves all resources with pagination and minimal data.
     *
     * @param requestContext Optional user context.
     * @param page           Page number (default = 0).
     * @param size           Page size (default = 10).
     * @return Paginated list of resources.
     */
    @Operation(summary = "Fetch paginated resources (minimal data)",
            description = "Fetches resources with minimal data and pagination.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated resources")
    @GetMapping("/paged")
    ResponseEntity<List<MinDto>> getAllPaged(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    /**
     * Retrieves all resources with full data.
     *
     * @param requestContext Optional user context.
     * @return List of resources.
     */
    @Operation(summary = "Fetch all resources (full data)",
            description = "Retrieves all resources with their full data representation.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved resources")
    @GetMapping("/full")
    ResponseEntity<List<FullDto>> getAllFull(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Retrieves all resources with full data and pagination.
     *
     * @param requestContext Optional user context.
     * @param page           Page number (default = 0).
     * @param size           Page size (default = 10).
     * @return Paginated list of resources.
     */
    @Operation(summary = "Fetch paginated resources (full data)",
            description = "Fetches resources with full data and pagination.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated resources")
    @GetMapping("/full/paged")
    ResponseEntity<List<FullDto>> getAllFullPaged(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    /**
     * Retrieves a resource by its ID.
     *
     * @param requestContext Optional user context.
     * @param id             Resource identifier.
     * @return The resource if found, or 404 if not.
     */
    @Operation(summary = "Fetch resource by ID",
            description = "Fetches a single resource by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    @GetMapping("/{id}")
    ResponseEntity<FullDto> getById(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable I id);

    /**
     * Retrieves the total count of resources.
     *
     * @param requestContext Optional user context.
     * @return Total count of resources.
     */
    @Operation(summary = "Get total resource count",
            description = "Fetches the total number of resources available.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved resource count")
    @GetMapping("/count")
    ResponseEntity<Long> getCount(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Retrieves resources based on filter criteria.
     *
     * @param requestContext Optional user context.
     * @param criteria       Filter criteria.
     * @return List of matching resources.
     */
    @Operation(summary = "Fetch resources by filter criteria",
            description = "Fetches resources that match the given filter criteria.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered resources")
    @GetMapping("/filter")
    ResponseEntity<List<FullDto>> getFiltered(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam String criteria);

    /**
     * Retrieves filtered resources with pagination.
     *
     * @param requestContext Optional user context.
     * @param criteria       Filter criteria.
     * @param page           Page number (default = 0).
     * @param size           Page size (default = 10).
     * @return Paginated list of filtered resources.
     */
    @Operation(summary = "Fetch paginated resources by filter criteria",
            description = "Fetches resources that match the given filter criteria, with pagination.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated filtered resources")
    @GetMapping("/filter/paged")
    ResponseEntity<List<FullDto>> getFilteredPaged(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam String criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    /**
     * Retrieves available filter criteria for resource filtering.
     *
     * @return Map of filter criteria.
     */
    @Operation(summary = "Fetch available filter criteria",
            description = "Retrieves the list of available filter criteria for searching resources.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filter criteria")
    @GetMapping("/filter/criteria")
    ResponseEntity<Map<String, String>> getFilterCriteria();
}