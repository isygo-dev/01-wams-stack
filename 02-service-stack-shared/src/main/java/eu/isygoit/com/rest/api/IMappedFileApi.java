package eu.isygoit.com.rest.api;

import eu.isygoit.dto.IFileUploadDto;

import java.io.Serializable;

/**
 * The interface Mapped file api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedFileApi<I extends Serializable, D extends IFileUploadDto>
        extends IMappedFileDownloadApi<I, D>, IMappedFileUploadApi<I, D> {
}
