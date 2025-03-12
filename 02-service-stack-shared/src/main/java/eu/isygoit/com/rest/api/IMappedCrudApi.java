package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IIdentifiableDto;

import java.io.Serializable;

/**
 * The interface Mapped crud api.
 *
 * @param <I> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 */
public interface IMappedCrudApi<I extends Serializable, M extends IIdentifiableDto, F extends M>
        extends IMappedCrudFetchApi<I, M, F>, IMappedCrudPersistApi<I, F>, IMappedCrudDeleteApi<I> {
}
