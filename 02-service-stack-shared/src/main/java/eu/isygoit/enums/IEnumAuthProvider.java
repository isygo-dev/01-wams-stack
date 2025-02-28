package eu.isygoit.enums;

/**
 * The interface Enum authentication.
 */
public interface IEnumAuthProvider {

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Embedded types.
         */
        EMBEDDED("embedded"),
        /**
         * Ldap types.
         */
        LDAP("ldap");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
