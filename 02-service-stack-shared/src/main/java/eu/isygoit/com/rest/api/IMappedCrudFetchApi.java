package eu.isygoit.com.rest.api;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.extendable.IdentifiableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The interface Mapped crud fetch api.
 *
 * @param <I> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 */
public interface IMappedCrudFetchApi<I extends Serializable, M extends IIdentifiableDto, F extends M> {

    /**
     * Find all response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    @Operation(summary = "Find all objects with minimal data (uses Min Dto)",
            description = "Find all objects with minimal data (uses Min Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "")
    ResponseEntity<List<M>> findAll(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Find all default response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    @Operation(summary = "Find all default tenant objects with minimal data (uses Min Dto)",
            description = "Find all default tenant objects with minimal data (uses Min Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/default")
    ResponseEntity<List<M>> findAllDefault(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);


    /**
     * Find all response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    @Operation(summary = "Find all objects with minimal data by page (uses Min Dto)",
            description = "Find all objects with minimal data by page (uses Min Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/{page}/{size}")
    ResponseEntity<List<M>> findAll(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                    @PathVariable(name = RestApiConstants.PAGE) Integer page,
                                    @PathVariable(name = RestApiConstants.SIZE) Integer size);

    /**
     * Find all full response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    @Operation(summary = "Find all objects with full data (uses Full Dto)",
            description = "Find all objects with full data (uses Full Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/full")
    ResponseEntity<List<F>> findAllFull(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Find all full response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    @Operation(summary = "Find all objects with full data by page (uses Full Dto)",
            description = "Find all objects with full data by page (uses Full Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/full/{page}/{size}")
    ResponseEntity<List<F>> findAllFull(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                        @PathVariable(name = RestApiConstants.PAGE) Integer page,
                                        @PathVariable(name = RestApiConstants.SIZE) Integer size);

    /**
     * Find by id response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    @Operation(summary = "Find object with full data by object identifier (uses Full Dto)",
            description = "Find object with full data by object identifier (uses Full Dto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/{id}")
    ResponseEntity<F> findById(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                               @PathVariable(name = RestApiConstants.ID) I id);

    /**
     * Gets count.
     *
     * @param requestContext the request context
     * @return the count
     */
    @Operation(summary = "Get objects count",
            description = "Get objects count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class))})
    })
    @GetMapping(path = "/count")
    ResponseEntity<Long> getCount(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext);

    /**
     * Find all filtered by criteria response entity.
     *
     * @param requestContext the request context
     * @param criteria       the criteria
     * @return the response entity
     */
    @Operation(summary = "Find all objects filtered by criteria cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5",
            description = "Find all objects filtered by criteria cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/filter")
    ResponseEntity<List<F>> findAllFilteredByCriteria(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                                      @RequestParam(name = RestApiConstants.CRITERIA) String criteria);

    /**
     * Find all filtered by criteria response entity.
     *
     * @param requestContext the request context
     * @param criteria       the criteria
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    @Operation(summary = "Find all objects filtered by criteria by page cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5",
            description = "Find all objects filtered by criteria by page cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiableDto.class))})
    })
    @GetMapping(path = "/filter/{page}/{size}")
    ResponseEntity<List<F>> findAllFilteredByCriteria(@RequestAttribute(value = JwtConstants.JWT_USER_CONTEXT, required = false) RequestContextDto requestContext,
                                                      @RequestParam(name = RestApiConstants.CRITERIA) String criteria,
                                                      @PathVariable(name = RestApiConstants.PAGE) Integer page,
                                                      @PathVariable(name = RestApiConstants.SIZE) Integer size);

    /**
     * Find all filter criteria response entity.
     *
     * @return the response entity
     */
    @Operation(summary = "Find all filter criteria",
            description = "Find all filter criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Api executed successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))})
    })
    @GetMapping(path = "/filter/criteria")
    ResponseEntity<Map<String, String>> findAllFilterCriteria();
}
