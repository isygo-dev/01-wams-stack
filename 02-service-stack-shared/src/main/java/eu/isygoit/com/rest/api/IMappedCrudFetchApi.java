package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdAssignableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for CRUD fetch operations with pagination and filtering capabilities.
 *
 * @param <I> Type parameter for the identifier (must be Serializable)
 * @param <M> Type parameter for minimal DTO (extends IIdAssignableDto)
 * @param <F> Type parameter for full DTO (extends M)
 */
@Tag(name = "CRUD Fetch Operations", description = "API endpoints for fetching data with pagination and filtering")
@SecurityRequirement(name = "BearerAuth")
public interface IMappedCrudFetchApi<I extends Serializable, M extends IIdAssignableDto<I>, F extends M> {

    /**
     * Retrieves all objects with minimal data by page.
     *
     * @param requestContext User context from JWT token
     * @param page           Page number for pagination (0-based)
     * @param size           Number of items per page
     * @return List of minimal DTO objects
     */
    @Operation(summary = "Find all objects with pagination",
            description = "Retrieves all objects with minimal data using Min DTO with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved objects",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content)
    })
    @GetMapping(path = "")
    ResponseEntity<List<M>> findAll(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PAGE, required = false)
            @Parameter(description = "Page number (0-based)", example = "0") Integer page,
            @RequestParam(name = RestApiConstants.SIZE, required = false)
            @Parameter(description = "Number of items per page", example = "20") Integer size);

    /**
     * Retrieves all objects with full data.
     *
     * @param requestContext User context from JWT token
     * @param page           Page number for pagination (0-based)
     * @param size           Number of items per page
     * @return List of full DTO objects
     */
    @Operation(summary = "Find all objects with full data",
            description = "Retrieves all objects with complete data using Full DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved objects",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content)
    })
    @GetMapping(path = "/full")
    ResponseEntity<List<F>> findAllFull(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PAGE, required = false)
            @Parameter(description = "Page number (0-based)", example = "0") Integer page,
            @RequestParam(name = RestApiConstants.SIZE, required = false)
            @Parameter(description = "Number of items per page", example = "20") Integer size);

    /**
     * Retrieves a single object by its identifier.
     *
     * @param requestContext User context from JWT token
     * @param id             Object identifier
     * @return Single full DTO object
     */
    @Operation(summary = "Find object by ID",
            description = "Retrieves a single object with full data using Full DTO by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved object",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content)
    })
    @GetMapping(path = "/{id}")
    ResponseEntity<F> findById(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.ID)
            @Parameter(description = "Object identifier", example = "123") I id);

    /**
     * Retrieves the total count of objects.
     *
     * @param requestContext User context from JWT token
     * @return Total count of objects
     */
    @Operation(summary = "Get objects count",
            description = "Retrieves the total count of objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved count",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content)
    })
    @GetMapping(path = "/count")
    ResponseEntity<Long> getCount(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext);

    /**
     * Retrieves objects filtered by criteria with pagination.
     *
     * @param requestContext User context from JWT token
     * @param criteria       Filter criteria string
     * @param page           Page number for pagination (0-based)
     * @param size           Number of items per page
     * @return List of full DTO objects matching criteria
     */
    @Operation(summary = "Find objects by filter criteria",
            description = "Retrieves objects filtered by criteria with pagination. Format: cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved filtered objects",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid filter criteria or pagination parameters",
                    content = @Content)
    })
    @GetMapping(path = "/filter")
    ResponseEntity<List<F>> findAllFilteredByCriteria(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.CRITERIA)
            @Parameter(description = "Filter criteria", example = "name=John,OR age>18") String criteria,
            @RequestParam(name = RestApiConstants.PAGE, required = false)
            @Parameter(description = "Page number (0-based)", example = "0") Integer page,
            @RequestParam(name = RestApiConstants.SIZE, required = false)
            @Parameter(description = "Number of items per page", example = "20") Integer size);

    /**
     * Retrieves all available filter criteria.
     *
     * @return Map of filter criteria and their descriptions
     */
    @Operation(summary = "Get available filter criteria",
            description = "Retrieves all available filter criteria and their descriptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved filter criteria",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content)
    })
    @GetMapping(path = "/filter/criteria")
    ResponseEntity<Map<String, String>> getAnnotatedCriteria();
}