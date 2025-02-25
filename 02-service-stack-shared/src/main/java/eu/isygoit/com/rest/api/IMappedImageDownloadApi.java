package eu.isygoit.com.rest.api;


import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.io.IOException;
import java.io.Serializable;

/**
 * The interface Mapped image download api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedImageDownloadApi<I extends Serializable, D extends IIdentifiableDto & IImageUploadDto> {

    /**
     * Download image response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     * @throws IOException the io exception
     */
    @Operation(summary = "Download the image by linked object identifier",
            description = "Download the image by linked object identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Resource.class))})
    })
    @GetMapping(path = "/image/download/{id}")
    ResponseEntity<Resource> downloadImage(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                           @PathVariable(name = RestApiConstants.ID) I id) throws IOException;
}
