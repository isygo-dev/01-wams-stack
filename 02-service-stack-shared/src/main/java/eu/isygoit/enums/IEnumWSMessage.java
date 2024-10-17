package eu.isygoit.enums;

/**
 * The interface Enum ws message.
 */
public interface IEnumWSMessage {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Message types.
         */
        MESSAGE("MESSAGE"),
        /**
         * Status types.
         */
        STATUS("STATUS");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
