package eu.isygoit.service;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.NextCodeDto;
import eu.isygoit.dto.common.RequestContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The interface Remote next code api.
 */
public interface IRemoteNextCodeService {

    /**
     * Generate next code response entity.
     *
     * @param requestContext the request context
     * @param tenant         the tenant
     * @param entity         the entity
     * @param attribute      the attribute
     * @return the response entity
     */
    @Operation(summary = "Generate next incremental code",
            description = "For a given tenant, entity and attribute, generate the next incremental code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully, and returns the new incremental code",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(name = "Generated code", implementation = String.class))})
    })
    @GetMapping(path = "/incremental/next")
    ResponseEntity<String> generateNextCode(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                            @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                            @RequestParam(name = RestApiConstants.ENTITY) String entity,
                                            @RequestParam(name = RestApiConstants.ATTRIBUTE) String attribute);

    /**
     * Subscribe next code response entity.
     *
     * @param tenant            the tenant
     * @param incrementalConfig the incremental config
     * @return the response entity
     */
    @Operation(summary = "Subscribe next incremental code generator",
            description = "For a given tenant, entity and attribute, subscribe the next incremental code generator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(name = "Next code generator config", implementation = NextCodeDto.class))})
    })
    @PostMapping(path = "/incremental/config")
    ResponseEntity<String> subscribeNextCode(//@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                             @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                             @Valid @RequestBody NextCodeDto incrementalConfig);
}