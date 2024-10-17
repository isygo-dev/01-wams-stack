package eu.isygoit.encrypt.generator;

import eu.isygoit.enums.IEnumCharSet;

/**
 * The type Resizable key generator.
 */
public class ResizableKeyGenerator extends AbstractKeyGenerator {

    /**
     * Next guid string.
     *
     * @param length      the length
     * @param charSetType the char set type
     * @return the string
     */
    public String nextGuid(int length, IEnumCharSet.Types charSetType) {
        this.setBufferlength(length);
        return nextGuid(charSetType);
    }
}
