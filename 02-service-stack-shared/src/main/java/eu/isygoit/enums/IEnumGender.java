package eu.isygoit.enums;

/**
 * The interface Enum gender.
 */
public interface IEnumGender {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 20;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Male types.
         */
        MALE("Male"),
        /**
         * Female types.
         */
        FEMALE("Female"),
        /**
         * Other types.
         */
        OTHER("Other");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
