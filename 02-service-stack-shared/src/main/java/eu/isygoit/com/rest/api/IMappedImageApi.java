package eu.isygoit.com.rest.api;


import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;

import java.io.Serializable;

/**
 * The interface Mapped image api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedImageApi<I extends Serializable, D extends IIdAssignableDto<I> & IImageUploadDto>
        extends IMappedImageDownloadApi<I, D>, IMappedImageUploadApi<I, D> {
}
