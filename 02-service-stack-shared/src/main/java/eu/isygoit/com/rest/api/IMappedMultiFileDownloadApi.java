package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Mapped multi file download api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IMappedMultiFileDownloadApi<L extends LinkedFileMinDto, I> {

    /**
     * Download response entity.
     
     * @param parentId       the parent id
     * @param fileId         the file id
     * @param version        the version
     * @return the response entity
     */
    @Operation(summary = "Download a multi-file by object id and file id",
            description = "Download a multi-file by object id and file id")
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
    @GetMapping(path = "/multi-files/download")
    ResponseEntity<Resource> download(
                                      @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                      @RequestParam(name = RestApiConstants.FILE_ID) I fileId,
                                      @RequestParam(name = RestApiConstants.VERSION) Long version);
}
