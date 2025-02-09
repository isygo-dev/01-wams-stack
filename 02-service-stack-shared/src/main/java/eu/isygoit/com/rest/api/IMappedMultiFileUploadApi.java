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

import java.io.Serializable;
import java.util.List;

/**
 * The interface for managing file upload associated with an object.
 *
 * @param <L> the type of the linked file DTO
 * @param <I> the type of the identifier for the parent object and file
 */
public interface IMappedMultiFileUploadApi<L extends LinkedFileMinDto, I extends Serializable> {

    /**
     * Upload multiple files for a specific object.
     *
     * @param requestContext the context of the current request or user
     * @param parentId       the identifier of the parent object
     * @param files          the array of files to be uploaded
     * @return a ResponseEntity containing the list of linked file DTOs for the uploaded files
     */
    @Operation(
            summary = "Upload multiple files associated with an object",
            description = "Upload a list of additional files for an object by specifying the parent ID and file array."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Files uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LinkedFileMinDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, one or more files or parameters are missing or incorrect",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping(path = "/multi-files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> upload(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestPart(name = RestApiConstants.FILES) MultipartFile[] files
    );

    /**
     * Upload a single file for a specific object.
     *
     * @param requestContext the context of the current request or user
     * @param parentId       the identifier of the parent object
     * @param file           the file to be uploaded
     * @return a ResponseEntity containing the list of linked file DTOs for the uploaded file
     */
    @Operation(
            summary = "Upload a single file associated with an object",
            description = "Upload a single additional file for an object by specifying the parent ID and the file."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LinkedFileMinDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request, missing or incorrect parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping(path = "/multi-files/upload/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<L>> upload(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) RequestContextDto requestContext,
            @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
            @RequestPart(name = RestApiConstants.FILES) MultipartFile file
    );
}