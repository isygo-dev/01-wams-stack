package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IIdentifiableDto;

import java.io.Serializable;

/**
 * The interface Mapped crud api.
 *
 * @param <I>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 */
public interface IMappedCrudApi<I extends Serializable, MIND extends IIdentifiableDto, FULLD extends MIND>
        extends IMappedCrudFetchApi<I, MIND, FULLD>, IMappedCrudPersistApi<I, FULLD>, IMappedCrudDeleteApi<I> {
}
