package eu.isygoit.com.rest.api;

import eu.isygoit.dto.common.LinkedFileMinDto;

import java.io.Serializable;

/**
 * The interface Mapped multi file api.
 *
 * @param <L> the type parameter
 * @param <I> the type parameter
 */
public interface IMappedMultiFileApi<L extends LinkedFileMinDto, I extends Serializable>
        extends IMappedMultiFileDeleteApi<L, I>, IMappedMultiFileUploadApi<L, I>, IMappedMultiFileDownloadApi<L, I> {
}
