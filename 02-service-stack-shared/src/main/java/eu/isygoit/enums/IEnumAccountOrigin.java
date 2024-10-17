package eu.isygoit.enums;

/**
 * The interface Enum account origin.
 */
public interface IEnumAccountOrigin {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * Sys admin types.
         */
        SYS_ADMIN("SYS_ADMIN"),
        /**
         * Dom admin types.
         */
        DOM_ADMIN("DOM_ADMIN"),
        /**
         * Signup types.
         */
        SIGNUP("Signup"),
        /**
         * Employee types.
         */
        EMPLOYEE("Employee"),
        /**
         * Resume types.
         */
        RESUME("Resume");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
