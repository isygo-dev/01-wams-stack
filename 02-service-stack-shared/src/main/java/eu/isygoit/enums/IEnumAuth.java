package eu.isygoit.enums;

/**
 * The interface Enum auth.
 */
public interface IEnumAuth {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * Token types.
         */
        TOKEN("Auth_by_token"),
        /**
         * Pwd types.
         */
        PWD("Auth_by_login_pwd"),
        /**
         * Otp types.
         */
        OTP("Auth_by_key"),
        /**
         * Qrc types.
         */
        QRC("Auth_by_QRC");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
