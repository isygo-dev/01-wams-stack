package eu.isygoit.encrypt.generator;

import eu.isygoit.enums.IEnumCharSet;

/**
 * The interface Key generator.
 */
public interface IKeyGenerator {
    /**
     * Current guid string.
     *
     * @return the string
     */
    String currentGuid();

    /**
     * Next guid string.
     *
     * @return the string
     */
    String nextGuid();

    /**
     * Next guid string.
     *
     * @param charSetType the char set type
     * @return the string
     */
    String nextGuid(IEnumCharSet.Types charSetType);
}
