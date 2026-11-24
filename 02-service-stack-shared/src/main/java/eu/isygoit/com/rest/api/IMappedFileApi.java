package eu.isygoit.com.rest.api;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * The interface Mapped file api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedFileApi<I extends Serializable, D extends IFileUploadDto>
        extends IMappedFileDownloadApi<I, D>, IMappedFileUploadApi<I, D>, IMappedFileDeleteApi<I, D> {

    /**
     * Update the category of the file of an object.
     *
     * @param parentId the parent id
     * @param category the new category (blank clears it)
     * @return true when stored
     */
    @Operation(summary = "Update the category of the file of an object",
            description = "Update the category of the file of an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Category successfully updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "404", description = "Object not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(path = "/file/category")
    ResponseEntity<Boolean> updateFileCategory(
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestParam(name = "category", required = false) String category);
}
