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

import java.io.IOException;


/**
 * The interface Linked file api.
 *
 * @param <D> the type parameter
 */
public interface ILinkedFileApi<D extends IFileUploadDto> {

    /**
     * Upload response entity.
     *
     * @param requestContext the request context
     * @param linkedFile     the linked file
     * @return the response entity
     * @throws IOException the io exception
     */
    @Operation(summary = "Upload linked file Api",
            description = "Upload linked file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileResponseDto.class))})
    })
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<LinkedFileResponseDto> upload(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                                 @ModelAttribute("linkedFile") D linkedFile) throws IOException;

    /**
     * Download response entity.
     *
     * @param requestContext the request context
     * @param tenant         the tenant
     * @param code           the code
     * @return the response entity
     * @throws IOException the io exception
     */
    @Operation(summary = "Download linked file Api",
            description = "Download linked file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Resource.class))})
    })
    @GetMapping(path = "/download", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Resource> download(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                      @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                      @RequestParam(name = RestApiConstants.CODE) String code) throws IOException;

    /**
     * Delete file response entity.
     *
     * @param requestContext the request context
     * @param tenant         the tenant
     * @param code           the code
     * @return the response entity
     */
    @Operation(summary = "Delete linked file Api",
            description = "Delete linked file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))})
    })
    @DeleteMapping(path = "/deleteFile")
    ResponseEntity<Boolean> deleteFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                       @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                       @RequestParam(name = RestApiConstants.CODE) String code);

}
