package eu.isygoit.dto;


/**
 * The interface Identifiable dto.
 *
 * @param <I> the type parameter
 */
public interface IIdentifiableDto<I> extends IDto {

    /**
     * Gets id.
     *
     * @return the id
     */
    I getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(I id);
}
