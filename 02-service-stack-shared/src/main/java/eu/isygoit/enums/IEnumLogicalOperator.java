package eu.isygoit.enums;

/**
 * The interface Enum logical ope.
 */
public interface IEnumLogicalOperator {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * And types.
         */
        AND("AND"),
        /**
         * Or types.
         */
        OR("OR");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
