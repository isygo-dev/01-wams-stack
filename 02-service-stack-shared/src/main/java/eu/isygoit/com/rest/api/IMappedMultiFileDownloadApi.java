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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * The interface for handling the download of additional files associated with an object.
 *
 * @param <L> the type of the linked file DTO
 * @param <I> the type of the identifier for the files and parent objects
 */
public interface IMappedMultiFileDownloadApi<L extends LinkedFileMinDto, I extends Serializable> {

    /**
     * Downloads a file associated with a given object.
     *
     * @param requestContext the context of the current request or user
     * @param parentId       the identifier of the parent object
     * @param fileId         the identifier of the file to be downloaded
     * @param version        the version of the file to be downloaded
     * @return a ResponseEntity containing the file resource and HTTP status
     */
    @Operation(
            summary = "Download a file associated with an object",
            description = "Downloads a specific file linked to a parent object, identified by file ID and version."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File downloaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LinkedFileMinDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "File or object not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Bad request, invalid parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping(path = "/multi-files/download")
    ResponseEntity<Resource> download(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestParam(name = RestApiConstants.FILE_ID) I fileId,
            @RequestParam(name = RestApiConstants.VERSION) Long version
    );
}