package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.io.Serializable;

/**
 * Generic API for deleting resources by ID.
 *
 * @param <I> Type of the resource identifier (must be serializable).
 */
public interface IMappedCrudDeleteApi<I extends Serializable> {

    /**
     * Deletes a resource by its identifier.
     *
     * @param requestContext Optional user context.
     * @param id             Unique identifier of the resource.
     * @return HTTP 204 No Content if deleted successfully, or 404 if resource is not found.
     */
    @Operation(summary = "Delete resource by ID",
            description = "Deletes a resource using its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or malformed ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found with the provided ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    ResponseEntity<String> delete(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable I id);
}