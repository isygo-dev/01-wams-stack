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
import org.springframework.web.bind.annotation.RequestAttribute;
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
    @Operation(summary = "Delete additional file for an object Api",
            description = "Delete additional file for an object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = LinkedFileMinDto.class))})
    })
    @DeleteMapping(path = "/multi-files")
    ResponseEntity<Boolean> deleteAdditionalFile(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT) ContextRequestDto requestContext,
                                                 @RequestParam(name = RestApiConstants.PARENT_ID) I parentId,
                                                 @RequestParam(name = RestApiConstants.FILE_ID) I fileId);
}
