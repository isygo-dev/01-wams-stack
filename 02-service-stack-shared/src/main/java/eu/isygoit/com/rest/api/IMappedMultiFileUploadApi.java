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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * The interface Mapped multi file upload api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IMappedMultiFileUploadApi<L extends LinkedFileMinDto, I> {

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
}
