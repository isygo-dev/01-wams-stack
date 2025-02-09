package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.common.LinkedFileResponseDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for managing linked files.
 *
 * @param <D> Type parameter extending {@link IFileUploadDto}.
 */
public interface ILinkedFileApi<D extends IFileUploadDto> {

    /**
     * Uploads a linked file.
     *
     * @param linkedFile File to be uploaded.
     * @return Response containing file details.
     */
    @Operation(summary = "Upload linked file",
            description = "Uploads a linked file to the server and returns file details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file data or missing parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<LinkedFileResponseDto> upload(@ModelAttribute D linkedFile);

    /**
     * Downloads a linked file by domain and code.
     *
     * @param requestContext Optional user context.
     * @param domain         Domain name associated with the file.
     * @param code           Unique file identifier.
     * @return Response containing the requested file as a resource.
     */
    @Operation(summary = "Download linked file",
            description = "Downloads a linked file by the specified domain and unique file code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(implementation = Resource.class))),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(path = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> download(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(RestApiConstants.DOMAIN_NAME) String domain,
            @RequestParam(RestApiConstants.CODE) String code);

    /**
     * Deletes a linked file.
     *
     * @param requestContext Optional user context.
     * @param domain         Domain name associated with the file.
     * @param code           Unique file identifier.
     * @return Response indicating whether the deletion was successful.
     */
    @Operation(summary = "Delete linked file",
            description = "Deletes a linked file by the specified domain and unique file code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(path = "/delete")
    ResponseEntity<Boolean> delete(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(RestApiConstants.DOMAIN_NAME) String domain,
            @RequestParam(RestApiConstants.CODE) String code);
}