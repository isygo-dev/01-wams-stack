package eu.isygoit.enums;


/**
 * The interface Enum notification.
 */
public interface IEnumNotification {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 20;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * New task types.
         */
        NEW_TASK(""), // new todo task
        /**
         * Survey answer types.
         */
        SURVEY_ANSWER(""),
        /**
         * Control assignment types.
         */
        CONTROL_ASSIGNMENT(""),
        /**
         * Control unassignment types.
         */
        CONTROL_UNASSIGNMENT(""),
        /**
         * New tzdocument types.
         */
        NEW_TZDOCUMENT(""),
        /**
         * New article created types.
         */
        NEW_ARTICLE_CREATED(""),
        /**
         * Lock control types.
         */
        LOCK_CONTROL(""),
        /**
         * Unlock control types.
         */
        UNLOCK_CONTROL(""),
        /**
         * New catalogue types.
         */
        NEW_CATALOGUE(""),
        /**
         * Change role types.
         */
        CHANGE_ROLE(""),
        /**
         * Start survey types.
         */
        START_SURVEY(""),
        /**
         * Submit survey types.
         */
        SUBMIT_SURVEY("");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return this.meaning;
        }
    }
}
