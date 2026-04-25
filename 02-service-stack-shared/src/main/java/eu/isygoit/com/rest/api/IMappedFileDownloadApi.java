package eu.isygoit.com.rest.api;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * The interface Mapped file download api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedFileDownloadApi<I extends Serializable, D extends IFileUploadDto> {


    /**
     * Download file response entity.
     *
     * @param id      the id
     * @param version the version
     * @return the response entity
     */
    @Operation(summary = "Download a file by object id and version",
            description = "Download a file by object id and version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File successfully downloaded",
                    content = {@Content(mediaType = "application/octet-stream",
                            schema = @Schema(implementation = Resource.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "File or object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @GetMapping(path = "/file/download/{id}")
    ResponseEntity<Resource> downloadFile(

            @PathVariable(name = RestApiConstants.ID) I id,
            @RequestParam(name = RestApiConstants.VERSION) Long version);
}
