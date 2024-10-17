package eu.isygoit.enums;

/**
 * The interface Enum media.
 */
public interface IEnumMedia {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;
    /**
     * The constant APPLICATION_JSON.
     */
    String APPLICATION_JSON = "application/json";

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Application json types.
         */
        APPLICATION_JSON(IEnumMedia.APPLICATION_JSON);

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
