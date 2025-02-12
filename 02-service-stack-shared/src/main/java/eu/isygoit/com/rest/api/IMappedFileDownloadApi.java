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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
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
     * @param requestContext the request context
     * @param id             the id
     * @param version        the version
     * @return the response entity
     */
    @Operation(summary = "Download a file by object id and version Api",
            description = "Download a file by object id and version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Resource.class))})
    })
    @GetMapping(path = "/file/download")
    ResponseEntity<Resource> downloadFile(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.ID) I id,
            @RequestParam(name = RestApiConstants.VERSION) Long version);
}
