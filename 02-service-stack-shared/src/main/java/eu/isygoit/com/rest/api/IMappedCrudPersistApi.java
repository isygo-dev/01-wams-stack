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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

/**
 * Generic CRUD API for managing persistent entities.
 *
 * @param <I> Type of the identifier.
 * @param <D> Type of the DTO implementing {@link IIdentifiableDto}.
 */
public interface IMappedCrudPersistApi<I extends Serializable, D extends IIdentifiableDto> {

    /**
     * Creates a new entity.
     *
     * @param dto The entity data to create.
     * @return The created entity with its identifier.
     */
    @Operation(
            summary = "Create a new entity",
            description = "Creates a new entity and returns the persisted representation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Entity created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<D> create(@Valid @RequestBody D dto);

    /**
     * Updates an existing entity.
     *
     * @param id  The identifier of the entity to update.
     * @param dto The updated entity data.
     * @return The updated entity.
     */
    @Operation(
            summary = "Update an existing entity",
            description = "Updates an existing entity and returns the modified representation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entity updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    ResponseEntity<D> update(
            @PathVariable(name = RestApiConstants.ID) I id,
            @Valid @RequestBody D dto
    );

}