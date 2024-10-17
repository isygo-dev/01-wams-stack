package eu.isygoit.enums;

/**
 * The interface Enum language level type.
 */
public interface IEnumLanguageLevelType {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 12;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Fluent types.
         */
        FLUENT("Fluent"),
        /**
         * Alright types.
         */
        ALRIGHT("Alright"),
        /**
         * Good types.
         */
        GOOD("Good"),
        /**
         * Intermediate types.
         */
        INTERMEDIATE("Intermediate"),
        /**
         * Beginner types.
         */
        BEGINNER("Beginner"),
        ;
        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}


