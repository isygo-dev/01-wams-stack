package eu.isygoit.service.token;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.dto.data.TokenRequestDto;
import eu.isygoit.enums.IEnumAppToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for managing authentication tokens.
 * Provides endpoints for generating and validating tokens.
 */
public interface TokenServiceApi {

    /**
     * Creates a new authentication token based on the provided domain, application, and token type.
     *
     * @param domain          The domain name used for identifying the service (e.g., "example.com").
     * @param application     The application name that is requesting the token (e.g., "my-app").
     * @param tokenType       The type of token being requested (e.g., "access" or "refresh").
     * @param tokenRequestDto The token request payload, containing necessary details for token creation.
     * @return A response entity containing the generated token details in the form of TokenDto.
     */
    @Operation(
            summary = "Generate a new authentication token",
            description = "Creates an authentication token based on the provided domain, application, and token type."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{domain}/{application}/{tokenType}")
    // URL path containing domain, application, and tokenType
    ResponseEntity<TokenDto> generateToken(
            @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain, // Extract domain from the path
            @PathVariable(name = RestApiConstants.APPLICATION) String application, // Extract application from the path
            @PathVariable(name = RestApiConstants.TOKEN_TYPE) IEnumAppToken.Types tokenType, // Extract token type from the path
            @Valid @RequestBody TokenRequestDto tokenRequestDto // The request body contains token generation details
    );

    /**
     * Validates whether the given token is still valid for the specified domain, application, and subject.
     *
     * @param requestContext The optional request context, containing user-specific information like JWT details.
     * @param domain         The domain name to validate the token against.
     * @param application    The application for which the token is being validated.
     * @param tokenType      The token type (access, refresh, etc.).
     * @param token          The actual token string that is being validated.
     * @param subject        The subject of the token (e.g., user ID).
     * @return A response entity indicating the result of the validation (true if valid, false otherwise).
     */
    @Operation(
            summary = "Validate an authentication token",
            description = "Checks if a given token is valid for the specified domain, application, and user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid token format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request"),
            @ApiResponse(responseCode = "404", description = "Token not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/validate/{domain}/{application}/{tokenType}/{token}/{subject}")
    // URL path for token validation
    ResponseEntity<Boolean> validateToken(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext, // Optional request context
            @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain, // Extract domain from the path
            @PathVariable(name = RestApiConstants.APPLICATION) String application, // Extract application from the path
            @PathVariable(name = RestApiConstants.TOKEN_TYPE) IEnumAppToken.Types tokenType, // Extract token type from the path
            @PathVariable(name = RestApiConstants.TOKEN) String token, // Extract token from the path
            @PathVariable(name = RestApiConstants.SUBJECT) String subject // Extract subject from the path
    );
}