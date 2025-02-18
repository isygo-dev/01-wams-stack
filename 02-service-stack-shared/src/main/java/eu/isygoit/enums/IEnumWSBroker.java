package eu.isygoit.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * The interface Enum ws broker.
 */
public interface IEnumWSBroker {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * Gets broker.
     *
     * @param destination the destination
     * @return the broker
     */
    public static IEnumWSBroker.Types getBroker(String destination) {
        return Arrays.stream(IEnumWSBroker.Types.values())
                .filter(type -> StringUtils.containsIgnoreCase(destination, "/" + type.name().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * User types.
         */
        USER("user"),
        /**
         * Group types.
         */
        GROUP("group"),
        /**
         * All types.
         */
        ALL("all");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
