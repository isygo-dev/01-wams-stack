package eu.isygoit.service;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.enums.IEnumCharSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * The interface Key service api.
 */
public interface KeyServiceApi {

    /**
     * Generate random key response entity.
     *
     * @param requestContext the request context
     * @param length         the length
     * @param charSetType    the char set type
     * @return the response entity
     */
    @Operation(summary = "Generate random key Api",
            description = "Generate random key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))})
    })
    @PostMapping(path = "/random/new")
    ResponseEntity<String> newRandomKey(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                        @RequestParam(name = RestApiConstants.LENGTH) Integer length,
                                        @RequestParam(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Renew key by name response entity.
     *
     * @param requestContext the request context
     * @param tenant         the tenant
     * @param keyName        the key name
     * @param length         the length
     * @param charSetType    the char set type
     * @return the response entity
     */
    @Operation(summary = "Renew key by name Api",
            description = "Renew key by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))})
    })
    @PostMapping(path = "/random/renew")
    ResponseEntity<String> renewRandomKey(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                          @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                          @RequestParam(name = RestApiConstants.KEY_NAME) String keyName,
                                          @RequestParam(name = RestApiConstants.LENGTH) Integer length,
                                          @RequestParam(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Gets key by name.
     *
     * @param requestContext the request context
     * @param tenant         the tenant
     * @param keyName        the key name
     * @return the key by name
     */
    @Operation(summary = "Get key by name Api",
            description = "Get key by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))})
    })
    @GetMapping(path = "/random")
    ResponseEntity<String> getRandomKey(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                        @RequestParam(name = RestApiConstants.TENANT_NAME) String tenant,
                                        @RequestParam(name = RestApiConstants.KEY_NAME) String keyName);
}