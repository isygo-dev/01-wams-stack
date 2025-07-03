package eu.isygoit.dto;


/**
 * The interface Identifiable dto.
 *
 * @param <T> the type parameter
 */
public interface IIdAssignableDto<T> extends IDto {

    /**
     * Gets id.
     *
     * @return the id
     */
    T getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(T id);
}
