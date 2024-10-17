package eu.isygoit.enums;

/**
 * The interface Enum ws status.
 */
public interface IEnumWSStatus {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * Away types.
         */
        AWAY("AWAY"),
        /**
         * Busy types.
         */
        BUSY("BUSY"),
        /**
         * Do not disturb types.
         */
        DO_NOT_DISTURB("DO_NOT_DISTURB"),
        /**
         * Connected types.
         */
        CONNECTED("CONNECTED"),
        /**
         * Disconnected types.
         */
        DISCONNECTED("DISCONNECTED");


        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
