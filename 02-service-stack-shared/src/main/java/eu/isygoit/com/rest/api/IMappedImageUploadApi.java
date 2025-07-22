package eu.isygoit.com.rest.api;


import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdAssignableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The interface Mapped image upload api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedImageUploadApi<I extends Serializable, D extends IIdAssignableDto<I> & IImageUploadDto> {

    /**
     * Create with image response entity.
     *
     * @param requestContext the request context
     * @param file           the file
     * @param dto            the dto
     * @return the response entity
     */
    @Operation(summary = "Create a new object and upload linked image file",
            description = "Create a new object and upload linked image file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))})
    })
    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> createWithImage(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                      @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
                                      @RequestPart D dto);

    /**
     * Update with image response entity.
     *
     * @param requestContext the request context
     * @param file           the file
     * @param dto            the dto
     * @return the response entity
     */
    @Operation(summary = "Upload a new image file and update the linked object",
            description = "Upload a new image file and update the linked object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))})
    })
    @PutMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> updateWithImage(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                      @RequestParam(name = RestApiConstants.ID) I id,
                                      @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
                                      @RequestPart D dto);

    /**
     * Upload image response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @param file           the file
     * @return the response entity
     */
    @Operation(summary = "Upload a new image file and link it to an object with object identifier",
            description = "Upload a new image file and link it to an object with object identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdAssignableDto.class))})
    })
    @PostMapping(path = "/image/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> uploadImage(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                  @PathVariable(name = RestApiConstants.ID) I id,
                                  @RequestPart(name = RestApiConstants.FILE) MultipartFile file);
}
