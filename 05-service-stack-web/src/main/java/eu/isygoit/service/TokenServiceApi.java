package eu.isygoit.service;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.dto.data.TokenRequestDto;
import eu.isygoit.enums.IEnumToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The interface Token service api.
 */
public interface TokenServiceApi {

    /**
     * Create token by domain response entity.
     *
     * @param domain          the domain
     * @param application     the application
     * @param tokenType       the token type
     * @param tokenRequestDto the token request dto
     * @return the response entity
     */
    @Operation(summary = "createTokenByDomain Api",
            description = "createTokenByDomain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenDto.class))})
    })
    @PostMapping(path = "/{domain}/{application}/{tokenType}")
    ResponseEntity<TokenDto> createTokenByDomain(//@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                                 @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain,
                                                 @PathVariable(name = RestApiConstants.APPLICATION) String application,
                                                 @PathVariable(name = RestApiConstants.TOKEN_TYPE) IEnumToken.Types tokenType,
                                                 @Valid @RequestBody TokenRequestDto tokenRequestDto);

    /**
     * Is token valid response entity.
     *
     * @param requestContext the request context
     * @param domain         the domain
     * @param application    the application
     * @param tokenType      the token type
     * @param token          the token
     * @param subject        the subject
     * @return the response entity
     */
    @Operation(summary = "isTokenValid Api",
            description = "isTokenValid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))})
    })
    @GetMapping(path = "/valid/{domain}/{application}/{tokenType}/{token}/{subject}")
    ResponseEntity<Boolean> isTokenValid(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                         @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain,
                                         @PathVariable(name = RestApiConstants.APPLICATION) String application,
                                         @PathVariable(name = RestApiConstants.TOKEN_TYPE) IEnumToken.Types tokenType,
                                         @PathVariable(name = RestApiConstants.TOKEN) String token,
                                         @PathVariable(name = RestApiConstants.SUBJECT) String subject);
}