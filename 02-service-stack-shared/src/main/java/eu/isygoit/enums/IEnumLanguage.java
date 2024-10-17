package eu.isygoit.enums;


/**
 * The interface Enum language.
 */
public interface IEnumLanguage {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 2;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Fr types.
         */
        FR("fr"),
        /**
         * En types.
         */
        EN("en"),
        /**
         * It types.
         */
        IT("it"),
        /**
         * Sp types.
         */
        SP("sp"),
        /**
         * Ar types.
         */
        AR("ar"),
        /**
         * De types.
         */
        DE("de"); // ...

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}
