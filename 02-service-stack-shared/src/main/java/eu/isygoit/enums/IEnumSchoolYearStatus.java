package eu.isygoit.enums;

/**
 * The interface Enum school year status.
 */
public interface IEnumSchoolYearStatus {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Type.
     */
    enum Type implements IEnum {
        /**
         * Closed types.
         */
        CLOSED("CLOSED"),
        /**
         * Open types.
         */
        OPEN("OPEN"),
        /**
         * Planned types.
         */
        PLANNED("PLANNED");

        private final String meaning;

        Type(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}