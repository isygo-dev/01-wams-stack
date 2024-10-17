package eu.isygoit.enums;

/**
 * The interface Enum contact.
 */
public interface IEnumContact {
    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Mobile types.
         */
        MOBILE("Mobile")    //
        ,
        /**
         * Phone types.
         */
        PHONE("Phone")    //
        ,
        /**
         * Fax types.
         */
        FAX("Fax")        //
        ,
        /**
         * Email types.
         */
        EMAIL("Email");   //

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
