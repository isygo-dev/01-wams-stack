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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * The interface Upload multi file api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IUploadMultiFileApi<L extends LinkedFileMinDto, I> {


    /**
     * Upload additional files response entity.
     *
     * @param requestContext the request context
     * @param parentId       the parent id
     * @param files          the files
     * @return the response entity
     */
    @Operation(summary = "Upload additional files for an object Api",
            description = "Upload additional files for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))})
    })
    @PutMapping(path = "/multi-files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> uploadAdditionalFiles(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
                                                  @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                                  @RequestPart(name = RestApiConstants.FILES) MultipartFile[] files);


    /**
     * Upload additional file response entity.
     *
     * @param requestContext the request context
     * @param parentId       the parent id
     * @param file           the file
     * @return the response entity
     */
    @Operation(summary = "Upload additional file for an object Api",
            description = "Upload additional file for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))})
    })
    @PutMapping(path = "/multi-files/upload/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> uploadAdditionalFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
                                                 @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                                 @RequestPart(name = RestApiConstants.FILES) MultipartFile file);

    /**
     * Delete additional file response entity.
     *
     * @param requestContext the request context
     * @param parentId       the parent id
     * @param fileId         the file id
     * @return the response entity
     */
    @Operation(summary = "Delete additional file for an object Api",
            description = "Delete additional file for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))})
    })
    @DeleteMapping(path = "/multi-files")
    ResponseEntity<Boolean> deleteAdditionalFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
                                                 @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                                 @RequestParam(name = RestApiConstants.FILE_ID) I fileId);

    /**
     * Download response entity.
     *
     * @param requestContext the request context
     * @param parentId       the parent id
     * @param fileId         the file id
     * @param version        the version
     * @return the response entity
     */
    @Operation(summary = "download Api",
            description = "download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))})
    })
    @GetMapping(path = "/multi-files/download")
    ResponseEntity<Resource> download(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
                                      @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                      @RequestParam(name = RestApiConstants.FILE_ID) I fileId,
                                      @RequestParam(name = RestApiConstants.VERSION) Long version);
}
