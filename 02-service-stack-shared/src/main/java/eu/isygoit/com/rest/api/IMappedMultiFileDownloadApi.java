package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.LinkedFileMinDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.common.ResourceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Mapped multi file download api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IMappedMultiFileDownloadApi<L extends LinkedFileMinDto, I> {

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
