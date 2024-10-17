package eu.isygoit.enums;

/**
 * The interface Enum jwt auth result.
 */
public interface IEnumJwtAuthResult {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 20;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Not authorized types.
         */
        NOT_AUTHORIZED("NOT_AUTHORIZED"),
        /**
         * Authorized types.
         */
        AUTHORIZED("AUTHENTIFICATED"),
        /**
         * Usr perm locled types.
         */
        USR_PERM_LOCLED("USR_PERM_LOCLED"),
        /**
         * Usr temp locled types.
         */
        USR_TEMP_LOCLED("USR_TEMP_LOCLED"),
        /**
         * Srv temp locled types.
         */
        SRV_TEMP_LOCLED("SRV_TEMP_LOCLED"),
        /**
         * Password expired types.
         */
        PASSWORD_EXPIRED("PASSWORD_EXPIRED");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
