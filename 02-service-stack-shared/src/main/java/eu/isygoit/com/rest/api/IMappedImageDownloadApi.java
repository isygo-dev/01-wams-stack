package eu.isygoit.com.rest.api;


import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface Mapped image download api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedImageDownloadApi<I extends Serializable, D extends IIdAssignableDto<I> & IImageUploadDto> {

    /**
     * Download image response entity.
     *
     * @param id the id
     * @return the response entity
     * @throws IOException the io exception
     */
    @Operation(summary = "Download the image by linked object identifier",
            description = "Download the image by linked object identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Image successfully downloaded",
                    content = {@Content(mediaType = "application/octet-stream",
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Image or object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @GetMapping(path = "/image/download/{id}")
    ResponseEntity<Resource> downloadImage(
            @PathVariable(name = RestApiConstants.ID) I id) throws IOException;
}
