package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdAssignableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

/**
 * The interface Mapped crud delete api.
 *
 * @param <I> the type parameter
 */
public interface IMappedCrudDeleteApi<I> {

    /**
     * Delete response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    @Operation(summary = "Delete object by identifier Api",
            description = "Delete object by identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))})
    })
    @DeleteMapping(path = "/{id}")
    ResponseEntity<?> delete(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                             @PathVariable(name = RestApiConstants.ID) I id);
}
