package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Interface for CRUD delete operations.
 *
 * @param <I> Type parameter for the identifier (must be Serializable)
 */
@Tag(name = "CRUD Delete Operations", description = "API endpoints for deleting data")
@SecurityRequirement(name = "BearerAuth")
public interface IMappedCrudDeleteApi<I> {

    /**
     * Deletes a single object by its identifier.
     *
     * @param requestContext User context from JWT token
     * @param id             Object identifier
     * @return ResponseEntity with no content on success
     */
    @Operation(summary = "Delete object by ID",
            description = "Deletes a single object identified by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Object successfully deleted",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid identifier format",
                    content = @Content)
    })
    @DeleteMapping(path = "/{id}")
    ResponseEntity<?> delete(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.ID)
            @Parameter(description = "Object identifier", example = "123") I id);

    /**
     * Deletes multiple objects by their identifiers.
     *
     * @param requestContext User context from JWT token
     * @param ids            List of object identifiers to delete
     * @return ResponseEntity with no content on success
     */
    @Operation(summary = "Batch delete objects",
            description = "Deletes multiple objects identified by their unique identifiers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Objects successfully deleted",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Invalid identifiers or empty list",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "One or more objects not found",
                    content = @Content)
    })
    @DeleteMapping(path = "/batch")
    ResponseEntity<?> batchDelete(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) RequestContextDto requestContext,
            @RequestBody
            @Parameter(description = "List of object identifiers", example = "[123, 124, 125]") List<I> ids);
}