package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IIdAssignableDto;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.Serializable;

/**
 * Interface for mapped CRUD operations combining fetch, persist, and delete functionalities.
 *
 * @param <I> the type parameter for the identifier (must implement Serializable)
 * @param <M> the type parameter for the DTO (must implement IIdAssignableDto)
 * @param <F> the type parameter for the full DTO (extends M)
 */
@Tag(
        name = "Mapped CRUD API",
        description = "API for performing Create, Read, Update, and Delete (CRUD) operations on entities with mapped DTOs. Supports single and batch operations for fetching, persisting, and deleting entities."
)
public interface IMappedCrudApi<I extends Serializable, M extends IIdAssignableDto<I>, F extends M>
        extends IMappedCrudFetchApi<I, M, F>, IMappedCrudPersistApi<I, F>, IMappedCrudDeleteApi<I> {
}