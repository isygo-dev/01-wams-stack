package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * API for downloading files associated with specific entities.
 *
 * @param <I> Type of the identifier.
 * @param <D> Type of the DTO implementing {@link IFileUploadDto}.
 */
public interface IMappedFileDownloadApi<I extends Serializable, D extends IFileUploadDto> {

    /**
     * Downloads a file based on the entity identifier and version.
     *
     * @param requestContext The context of the request (e.g., user info from JWT).
     * @param id             The identifier of the entity.
     * @param version        The version of the file.
     * @return A ResponseEntity containing the file as a resource.
     */
    @Operation(
            summary = "Download a file by entity ID and version",
            description = "Downloads the file associated with the entity ID and version provided."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    @GetMapping(path = "/file/download", produces = "application/octet-stream")
    ResponseEntity<Resource> download(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.ID) I id,
            @RequestParam(name = RestApiConstants.VERSION) Long version
    );
}