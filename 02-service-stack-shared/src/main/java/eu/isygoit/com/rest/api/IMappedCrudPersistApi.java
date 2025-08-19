package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.extendable.IdAssignableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for CRUD persist operations (create and update).
 *
 * @param <I> Type parameter for the identifier (must be Serializable)
 * @param <D> Type parameter for the DTO (must implement IIdAssignableDto<I>)
 */
@Tag(name = "CRUD Persist Operations", description = "API endpoints for creating and updating data")
@SecurityRequirement(name = "BearerAuth")
public interface IMappedCrudPersistApi<I extends Serializable, D extends IIdAssignableDto<I>> {

    /**
     * Creates a new object.
     *
     * @param requestContext User context from JWT token
     * @param object         The object to create
     * @return ResponseEntity containing the created object with its assigned ID
     */
    @Operation(summary = "Create a new object",
            description = "Creates a new object and returns it with its assigned identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Object successfully created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid object data or validation failure",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "409",
                    description = "Conflict - Object already exists",
                    content = @Content)
    })
    @PostMapping(path = "", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> create(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) ContextRequestDto requestContext,
            @Valid @RequestBody
            @Parameter(description = "Object to create", required = true) D object);

    /**
     * Creates multiple objects in a single request.
     *
     * @param requestContext User context from JWT token
     * @param objects        List of objects to create
     * @return ResponseEntity containing the list of created objects with their assigned IDs
     */
    @Operation(summary = "Batch create objects",
            description = "Creates multiple objects in a single request and returns them with their assigned identifiers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Objects successfully created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid objects data, validation failure, or empty list",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "409",
                    description = "Conflict - One or more objects already exist",
                    content = @Content)
    })
    @PostMapping(path = "/batch", consumes = "application/json", produces = "application/json")
    ResponseEntity<List<D>> createBatch(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) ContextRequestDto requestContext,
            @Valid @RequestBody
            @Parameter(description = "List of objects to create", required = true, example = "[{id: null, ...}, {id: null, ...}]") List<D> objects);

    /**
     * Updates an existing object.
     *
     * @param requestContext User context from JWT token
     * @param id             Object identifier
     * @param object         The updated object data
     * @return ResponseEntity containing the updated object
     */
    @Operation(summary = "Update an existing object",
            description = "Updates an existing object identified by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Object successfully updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid object data or validation failure",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content)
    })
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> update(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false)
            @Parameter(description = "JWT user context", hidden = true) ContextRequestDto requestContext,
            @PathVariable(name = RestApiConstants.ID)
            @Parameter(description = "Object identifier", required = true, example = "123") I id,
            @Valid @RequestBody
            @Parameter(description = "Updated object data", required = true) D object);
}