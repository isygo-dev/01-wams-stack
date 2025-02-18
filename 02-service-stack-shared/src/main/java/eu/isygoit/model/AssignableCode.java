package eu.isygoit.model;

import org.springframework.util.StringUtils;

/**
 * The interface Codifiable.
 */
public interface AssignableCode {

    /**
     * Gets code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Sets code.
     *
     * @param code the code
     */
    void setCode(String code);


    /**
     * Has code boolean.
     *
     * @return the boolean
     */
    public default boolean hasCode() {
        return StringUtils.hasText(getCode());
    }
}

