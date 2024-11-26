package eu.isygoit.enums;

/**
 * The interface Enum criteria combiner.
 */
public interface IEnumCriteriaCombiner {


    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 8;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * And types.
         */
        AND("&", "AND"),
        /**
         * Or types.
         */
        OR("|", "OR");

        private final String symbol;
        private final String meaning;

        Types(String symbol, String meaning) {
            this.symbol = symbol;
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }

        /**
         * Symbol string.
         *
         * @return the string
         */
        public String symbol() {
            return symbol;
        }
    }
}