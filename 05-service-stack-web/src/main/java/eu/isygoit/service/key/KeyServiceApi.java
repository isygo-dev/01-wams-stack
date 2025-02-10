package eu.isygoit.service.key;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

/**
 * Interface for defining the Key Service API.
 * This service allows for generating, renewing, and retrieving keys using different parameters.
 */
public interface KeyServiceApi {

    /**
     * Generates a random key.
     *
     * @param requestContext the request context, contains user-related data (optional)
     * @param length         the length of the generated key
     * @param charSetType    the character set to use for key generation (e.g., alphanumeric, hexadecimal)
     * @return ResponseEntity containing the generated key as a string
     */
    @Operation(summary = "Generate a random key", description = "Generates a random key based on length and charset type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(path = "/generateRandomKey")
    ResponseEntity<String> generateRandomKey(
            // Using Optional to avoid null checks and improve clarity
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.LENGTH) Integer length,
            @PathVariable(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Renews a key by its name.
     *
     * @param requestContext the request context, contains user-related data (optional)
     * @param domain         the domain name where the key belongs
     * @param keyName        the name of the key to renew
     * @param length         the length of the renewed key
     * @param charSetType    the character set to use for the renewed key
     * @return ResponseEntity containing the renewed key as a string
     */
    @Operation(summary = "Renew key by name", description = "Renews an existing key by its name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key renewed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Key or domain not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping(path = "/renewKey")
    ResponseEntity<String> renewKeyByName(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain,
            @PathVariable(name = RestApiConstants.KEY_NAME) String keyName,
            @PathVariable(name = RestApiConstants.LENGTH) Integer length,
            @PathVariable(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Retrieves a key by its name.
     *
     * @param requestContext the request context, contains user-related data (optional)
     * @param domain         the domain name where the key belongs
     * @param keyName        the name of the key to retrieve
     * @return ResponseEntity containing the key as a string
     */
    @Operation(summary = "Retrieve key by name", description = "Fetches an existing key by its name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key fetched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Key not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(path = "/getKey")
    ResponseEntity<String> getKeyByName(
            @RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
            @PathVariable(name = RestApiConstants.DOMAIN_NAME) String domain,
            @PathVariable(name = RestApiConstants.KEY_NAME) String keyName);
}