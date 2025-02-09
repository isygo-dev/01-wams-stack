package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IFileUploadDto;
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
 * API for uploading and managing files associated with entities.
 *
 * @param <I> Type of the identifier.
 * @param <D> Type of the DTO implementing {@link IFileUploadDto}.
 */
public interface IMappedFileUploadApi<I extends Serializable, D extends IFileUploadDto> {

    /**
     * Uploads a new file for an existing entity.
     *
     * @param requestContext The context of the request (e.g., user info from JWT).
     * @param id             The identifier of the entity to associate with the uploaded file.
     * @param file           The file to upload.
     * @return The entity with the linked file.
     */
    @Operation(summary = "Upload a file for an existing entity",
            description = "Uploads a new file for an existing entity based on its ID and version.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(path = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> uploadFile(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.ID) I id,
            @Valid @RequestParam("file") MultipartFile file);

    /**
     * Creates a new entity and upload a linked file.
     *
     * @param requestContext The context of the request.
     * @param fileUpload     The entity and file to upload.
     * @return The created entity with the linked file.
     */
    @Operation(summary = "Create a new entity and upload a linked file",
            description = "Creates a new entity and upload the associated file.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entity created and file uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> createWithFile(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @ModelAttribute(RestApiConstants.FILE_UPLOAD) D fileUpload);

    /**
     * Updates an existing entity and upload a new linked file.
     *
     * @param requestContext The context of the request.
     * @param id             The identifier of the entity to update.
     * @param fileUpload     The updated entity and file to upload.
     * @return The updated entity with the new linked file.
     */
    @Operation(summary = "Upload a new file and update the linked entity",
            description = "Uploads a new file and updates the linked entity based on its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity updated and file uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentifiableDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<D> updateWithFile(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.ID) I id,
            @ModelAttribute(RestApiConstants.FILE_UPLOAD) D fileUpload);
}