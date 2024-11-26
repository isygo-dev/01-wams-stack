package eu.isygoit.com.rest.api;


import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;

/**
 * The interface Mapped image api.
 *
 * @param <I> the type parameter
 * @param <D> the type parameter
 */
public interface IMappedImageApi<I, D extends IIdentifiableDto & IImageUploadDto>
        extends IMappedImageDownloadApi<I, D>, IMappedImageUploadApi<I, D> {
}
