package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdentifiableDto;
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
 * The interface for handling image upload operations for an object.
 *
 * @param <I> the type of the identifier
 * @param <D> the type of the DTO, which must implement both IIdentifiableDto and IImageUploadDto
 */
public interface IMappedImageUploadApi<I extends Serializable, D extends IIdentifiableDto & IImageUploadDto> {

    /**
     * Creates a new object and upload an associated image file.
     *
     * @param requestContext the context of the current user or request
     * @param imageFile      the image file to be uploaded
     * @param dto            the data transfer object containing the object details
     * @return a ResponseEntity containing the created object and HTTP status
     */
    @Operation(
            summary = "Create a new object and upload its associated image",
            description = "Create a new object and upload an associated image file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Object created successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> createObjectWithImage(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @Valid @RequestParam("file") MultipartFile imageFile,
            @Valid @RequestPart("dto") D dto
    );

    /**
     * Updates an existing object by uploading a new image file.
     *
     * @param requestContext the context of the current user or request
     * @param imageFile      the new image file to upload
     * @param dto            the data transfer object containing updated object details
     * @return a ResponseEntity containing the updated object and HTTP status
     */
    @Operation(
            summary = "Update object with a new image",
            description = "Upload a new image file and associate it with an existing object"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Object updated successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PutMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> updateObjectWithImage(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @Valid @RequestParam("file") MultipartFile imageFile,
            @Valid @RequestPart("dto") D dto
    );

    /**
     * Uploads an image file and links it to an existing object by its identifier.
     *
     * @param requestContext the context of the current user or request
     * @param objectId       the unique identifier of the object
     * @param imageFile      the image file to be uploaded and linked
     * @return a ResponseEntity containing the object with its new image link
     */
    @Operation(
            summary = "Upload image and link it to an object by identifier",
            description = "Upload an image file and link it to an object identified by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Image uploaded and linked successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @PostMapping(path = "/image/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> uploadImageAndLinkToObject(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.ID) I objectId,
            @Valid @RequestParam("file") MultipartFile imageFile
    );
}