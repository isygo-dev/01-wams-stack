package eu.isygoit.service;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.common.PaginatedResponseDto;
import eu.isygoit.dto.common.RandomKeyDto;
import eu.isygoit.enums.IEnumCharSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * The interface Key api api.
 */
public interface RandomKeyServiceApi {

    /**
     * Generate random key response entity.
     *
     * @param length      the length
     * @param charSetType the char set type
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
    ResponseEntity<String> newRandomKey(
            @RequestParam(name = RestApiConstants.LENGTH) Integer length,
            @RequestParam(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Renew key by name response entity.
     *
     * @param keyName     the key name
     * @param length      the length
     * @param charSetType the char set type
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
    ResponseEntity<String> renewRandomKey(
            @RequestParam(name = RestApiConstants.KEY_NAME) String keyName,
            @RequestParam(name = RestApiConstants.LENGTH) Integer length,
            @RequestParam(name = RestApiConstants.CHAR_SET_TYPE) IEnumCharSet.Types charSetType);

    /**
     * Gets key by name.
     *
     * @param keyName the key name
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
    ResponseEntity<String> getRandomKey(
            @RequestParam(name = RestApiConstants.KEY_NAME) String keyName);

    @GetMapping("/list")
    ResponseEntity<PaginatedResponseDto<RandomKeyDto>> listRandomKeys(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size);

    @DeleteMapping("/{name}")
    ResponseEntity<Void> deleteRandomKey(
            @PathVariable(name = RestApiConstants.KEY_NAME) String keyName);
}