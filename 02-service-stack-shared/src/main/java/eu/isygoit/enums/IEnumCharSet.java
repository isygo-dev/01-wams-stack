package eu.isygoit.enums;

/**
 * The interface Enum char set.
 */
public interface IEnumCharSet {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Numeric types.
         */
        NUMERIC("NUMERIC"),
        /**
         * Alpha types.
         */
        ALPHA("ALPHA"),
        /**
         * Alphanum types.
         */
        ALPHANUM("ALPHANUM"),
        /**
         * All types.
         */
        ALL("ALL");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
