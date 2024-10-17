package eu.isygoit.enums;

/**
 * The interface Enum task reminder.
 */
public interface IEnumTaskReminder {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Daily types.
         */
        DAILY(""),
        /**
         * Weekly types.
         */
        WEEKLY(""),
        /**
         * Monthly types.
         */
        MONTHLY(""),
        /**
         * Yearly types.
         */
        YEARLY("");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}
