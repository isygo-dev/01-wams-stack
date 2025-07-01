package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdentifiableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

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
    ResponseEntity<D> create(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                             @Valid @RequestBody D object);

    @Operation(summary = "Create multiple objects",
            description = "Create multiple objects in a single request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Objects created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PostMapping(path = "/batch", consumes = "application/json", produces = "application/json")
    ResponseEntity<List<D>> createBatch(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @Valid @RequestBody List<D> objects);

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
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> update(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                             @PathVariable(name = RestApiConstants.ID) I id,
                             @Valid @RequestBody D object);
}
