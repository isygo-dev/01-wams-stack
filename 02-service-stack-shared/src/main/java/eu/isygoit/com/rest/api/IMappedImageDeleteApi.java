package eu.isygoit.com.rest.api;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IImageUploadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;

/**
 * The interface Mapped image delete api.
 *
 * @param <I> the id type
 * @param <D> the dto type
 */
public interface IMappedImageDeleteApi<I extends Serializable, D extends IImageUploadDto> {

    /**
     * Delete the image of an object (the stored file and the image path).
     *
     * @param id the object id
     * @return true when the image was removed
     */
    @Operation(summary = "Delete image for an object",
            description = "Delete the image of an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Image successfully deleted",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @DeleteMapping(path = "/image/delete/{id}")
    ResponseEntity<Boolean> deleteImage(@PathVariable(name = RestApiConstants.ID) I id);
}
