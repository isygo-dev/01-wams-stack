package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Mapped multi file delete api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IMappedMultiFileDeleteApi<L extends LinkedFileMinDto, I> {

    /**
     * Delete additional file response entity.
     *
     * @param requestContext the request context
     * @param parentId       the parent id
     * @param fileId         the file id
     * @return the response entity
     */
    @Operation(summary = "Delete additional file for an object",
            description = "Delete additional file for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File successfully deleted",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "File or object not found",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @DeleteMapping(path = "/multi-files")
    ResponseEntity<Boolean> deleteAdditionalFile(@RequestPart(value = JwtConstants.JWT_USER_CONTEXT) ContextRequestDto requestContext,
                                                 @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                                 @RequestParam(name = RestApiConstants.FILE_ID) I fileId);
}
