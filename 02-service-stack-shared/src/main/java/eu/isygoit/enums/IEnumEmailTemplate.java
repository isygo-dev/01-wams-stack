package eu.isygoit.enums;

/**
 * The interface Enum msg template name.
 */
public interface IEnumEmailTemplate {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 30;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * User created template types.
         */
        USER_CREATED_TEMPLATE("USER_CREATED_TEMPLATE"),
        /**
         * Forgot password template types.
         */
        FORGOT_PASSWORD_TEMPLATE("FORGOT_PASSWORD_TEMPLATE"),
        /**
         * Resume shared template types.
         */
        RESUME_SHARED_TEMPLATE("RESUME_SHARED_TEMPLATE"),
        /**
         * Auth otp template types.
         */
        AUTH_OTP_TEMPLATE("AUTH_OTP_TEMPLATE"),
        /**
         * Job offer shared template types.
         */
        JOB_OFFER_SHARED_TEMPLATE("JOB_OFFER_SHARED_TEMPLATE"),
        /**
         * Wfb updated template types.
         */
        WFB_UPDATED_TEMPLATE("WFB_UPDATED_TEMPLATE"),
        /**
         * New post published template types.
         */
        NEW_POST_PUBLISHED_TEMPLATE("NEW_POST_PUBLISHED_TEMPLATE"),
        /**
         * Unmanaged exception template types.
         */
        UNMANAGED_EXCEPTION_TEMPLATE("UNMANAGED_EXCEPTION_TEMPLATE"),
        /**
         * Password expire template types.
         */
        PASSWORD_EXPIRE_TEMPLATE("PASSWORD_EXPIRE_TEMPLATE");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
