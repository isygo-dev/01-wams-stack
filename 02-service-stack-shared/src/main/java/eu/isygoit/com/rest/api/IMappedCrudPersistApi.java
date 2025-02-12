package eu.isygoit.com.rest.api;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.extendable.IdentifiableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * The interface Mapped crud persist api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedCrudPersistApi<I extends Serializable, D extends IIdentifiableDto> {

    /**
     * Create response entity.
     *
     * @param object the object
     * @return the response entity
     */
    @Operation(summary = "Create a new object",
            description = "Create a new object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> create(//@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                             @Valid @RequestBody D object);

    /**
     * Update response entity.
     *
     * @param id     the id
     * @param object the object
     * @return the response entity
     */
    @Operation(summary = "Update an existing object",
            description = "Update an existing object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PutMapping(path = "", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> update(//@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                             @RequestParam(name = RestApiConstants.ID) I id,
                             @Valid @RequestBody D object);
}
