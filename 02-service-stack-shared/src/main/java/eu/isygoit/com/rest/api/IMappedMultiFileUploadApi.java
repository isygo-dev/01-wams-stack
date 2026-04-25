package eu.isygoit.com.rest.api;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.LinkedFileMinDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
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
     * @param parentId the parent id
     * @param files    the files
     * @return the response entity
     */
    @Operation(summary = "Upload additional files for an object",
            description = "Upload additional files for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Files successfully uploaded",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid files",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @PutMapping(path = "/multi-files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> uploadAdditionalFiles(
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestPart(name = RestApiConstants.FILES) MultipartFile[] files);


    /**
     * Upload additional file response entity.
     *
     * @param parentId the parent id
     * @param file     the file
     * @return the response entity
     */
    @Operation(summary = "Upload additional file for an object",
            description = "Upload additional file for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File successfully uploaded",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @PutMapping(path = "/multi-files/upload/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> uploadAdditionalFile(
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestPart(name = RestApiConstants.FILE) MultipartFile file);
}
