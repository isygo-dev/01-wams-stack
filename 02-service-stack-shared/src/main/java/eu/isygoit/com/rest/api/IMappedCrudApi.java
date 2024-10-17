package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IIdentifiableDto;

/**
 * The interface Mapped crud api.
 *
 * @param <I>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 */
public interface IMappedCrudApi<I, MIND extends IIdentifiableDto, FULLD extends MIND>
        extends IMappedFetchCrudApi<I, MIND, FULLD>, IMappedPersistCrudApi<I, FULLD>, IMappedDeleteCrudApi<I> {
}
