package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.LinkedFileMinDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * The interface for managing the deletion of additional files associated with an object.
 *
 * @param <L> the type of the linked file DTO
 * @param <I> the type of the identifier for the files and parent objects
 */
public interface IMappedMultiFileDeleteApi<L extends LinkedFileMinDto, I extends Serializable> {

    /**
     * Deletes an additional file associated with a given object.
     *
     * @param requestContext the context of the current request or user
     * @param parentId       the identifier of the parent object
     * @param fileId         the identifier of the file to be deleted
     * @return a ResponseEntity indicating success or failure of the deletion operation
     */
    @Operation(
            summary = "Delete an additional file associated with an object",
            description = "Deletes an additional file linked to a specific object by its identifiers."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "File successfully deleted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LinkedFileMinDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Bad request, invalid identifiers provided",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "File or object not found",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping(path = "/multi-files")
    ResponseEntity<Boolean> delete(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestParam(name = RestApiConstants.FILE_ID) I fileId
    );
}