package eu.isygoit.enums;

/**
 * The interface Enum account system status.
 */
public interface IEnumAccountSystemStatus {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Idle types.
         */
        IDLE("IDLE"),
        /**
         * Expired types.
         */
        EXPIRED("Expired"),
        /**
         * Registred types.
         */
        REGISTRED("Registred"),
        /**
         * The Tem locked.
         */
        TEM_LOCKED("Temporarily Locked"),
        /**
         * Locked types.
         */
        LOCKED("Locked");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
