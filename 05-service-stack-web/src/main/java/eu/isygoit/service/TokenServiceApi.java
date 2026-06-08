package eu.isygoit.service;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.dto.common.TokenRequestDto;
import eu.isygoit.enums.IEnumToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * The interface Token api api.
 */
public interface TokenServiceApi {

    /**
     * Create token by tenant response entity.
     *
     * @param audience        the audience
     * @param tokenType       the token type
     * @param tokenRequestDto the token request dto
     * @return the response entity
     */
    @Operation(summary = "buildToken Api",
            description = "buildToken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDto.class))})
    })
    @PostMapping(path = "/builder")
    ResponseEntity<TokenResponseDto> buildToken(
            @RequestParam(name = RestApiConstants.AUDIENCE) Set<String> audience,
            @RequestParam(name = RestApiConstants.TOKEN_TYPE) IEnumToken.Types tokenType,
            @Valid @RequestBody TokenRequestDto tokenRequestDto);

    /**
     * Is token valid response entity.
     *
     * @param audience  the audience
     * @param tokenType the token type
     * @param token     the token
     * @param subject   the subject
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
    @GetMapping(path = "/validation")
    ResponseEntity<Boolean> isTokenValid(
            @RequestParam(name = RestApiConstants.AUDIENCE) Set<String> audience,
            @RequestParam(name = RestApiConstants.TOKEN_TYPE) IEnumToken.Types tokenType,
            @RequestParam(name = RestApiConstants.TOKEN) String token,
            @RequestParam(name = RestApiConstants.SUBJECT) String subject);
}