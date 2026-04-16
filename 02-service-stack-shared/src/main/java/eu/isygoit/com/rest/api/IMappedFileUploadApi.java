package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.extendable.IdAssignableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The interface Mapped file upload api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedFileUploadApi<I extends Serializable, D extends IFileUploadDto> {

    /**
     * Upload file response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @param file           the file
     * @return the response entity
     */
    @Operation(summary = "Upload a file by object id",
            description = "Upload a file by object id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File successfully uploaded",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
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
    @PutMapping(path = "/file/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> uploadFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) ContextRequestDto requestContext,
                                 @PathVariable(name = RestApiConstants.ID) I id,
                                 @RequestPart(name = RestApiConstants.FILE) MultipartFile file);

    /**
     * Create with file response entity.
     *
     * @param requestContext the request context
     * @param file           the file
     * @param dto            the dto
     * @return the response entity
     */
    @Operation(summary = "Create a new object and upload linked file",
            description = "Create a new object and upload linked file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Object successfully created with file",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid object data or file",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> createWithFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) ContextRequestDto requestContext,
                                     @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
                                     @Valid @RequestPart D dto);


    /**
     * Update with file response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @param file           the file
     * @param dto            the dto
     * @return the response entity
     */
    @Operation(summary = "Upload a new file and update the linked object",
            description = "Upload a new file and update the linked object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Object and file successfully updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid object data or file",
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
    @PutMapping(path = "/file/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> updateWithFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) ContextRequestDto requestContext,
                                     @PathVariable(name = RestApiConstants.ID) I id,
                                     @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
                                     @Valid @RequestPart D dto);

}
