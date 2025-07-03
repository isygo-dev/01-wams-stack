package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IIdAssignableDto;

import java.io.Serializable;

/**
 * The interface Mapped crud api.
 *
 * @param <I> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 */
public interface IMappedCrudApi<I extends Serializable, M extends IIdAssignableDto, F extends M>
        extends IMappedCrudFetchApi<I, M, F>, IMappedCrudPersistApi<I, F>, IMappedCrudDeleteApi<I> {
}
