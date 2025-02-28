package eu.isygoit.enums;


/**
 * The interface Enum binary status.
 */
public interface IEnumEnabledBinaryStatus {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Enabled types.
         */
        ENABLED("ENABLED"),
        /**
         * Disabled types.
         */
        DISABLED("DISABLED");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}
