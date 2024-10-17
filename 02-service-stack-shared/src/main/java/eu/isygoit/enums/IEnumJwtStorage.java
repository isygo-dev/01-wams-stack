package eu.isygoit.enums;

/**
 * The interface Enum jwt storage.
 */
public interface IEnumJwtStorage {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * The Local.
         */
        LOCAL("Local storage"),
        /**
         * The Cookie.
         */
        COOKIE("Cookie storage");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }

}
