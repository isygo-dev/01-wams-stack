package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.io.IOException;
import java.io.Serializable;

/**
 * Interface for handling image downloads linked to a specific object identifier.
 *
 * @param <I> the type of the object identifier
 * @param <D> the type of the DTO with image upload capabilities
 */
public interface IMappedImageDownloadApi<I extends Serializable, D extends IIdentifiableDto & IImageUploadDto> {

    /**
     * Downloads an image associated with the given object identifier.
     *
     * @param requestContext the context of the incoming request (e.g., user information)
     * @param id             the unique identifier of the object linked to the image
     * @return a ResponseEntity containing the image as a resource
     * @throws IOException if there is an issue reading the image file
     */
    @Operation(
            summary = "Download image by object identifier",
            description = "Fetches and returns the image associated with the given object identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the image",
                    content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or request",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping(path = "/image/download/{id}")
    ResponseEntity<Resource> downloadImage(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.ID) I id
    ) throws IOException;
}