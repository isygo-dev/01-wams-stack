package eu.isygoit.enums;

/**
 * The interface Enum web token.
 */
public interface IEnumWebToken {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Bearer types.
         */
        Bearer("Bearer");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
