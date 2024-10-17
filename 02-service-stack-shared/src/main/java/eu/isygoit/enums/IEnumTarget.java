package eu.isygoit.enums;


/**
 * The interface Enum target.
 */
public interface IEnumTarget {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Company types.
         */
        COMPANY(""),    //All comany users
        /**
         * All companies types.
         */
        ALL_COMPANIES(""),  // All compnies
        /**
         * User types.
         */
        USER(""),
        /**
         * Amp role types.
         */
        AMP_ROLE(""),
        /**
         * Company role types.
         */
        COMPANY_ROLE("");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}
