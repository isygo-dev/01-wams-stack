package eu.isygoit.enums;

/**
 * The interface Enum operator.
 */
public interface IEnumOperator {


    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 8;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * Eq types.
         */
        EQ(" = ", "EQUAL"),
        /**
         * The Ne.
         */
        NE(" != ", "NOT EQUAL"),
        /**
         * The Gt.
         */
        GT(" > ", "GREATER THAN"),
        /**
         * The Ge.
         */
        GE(" >= ", "GREATER OR EQUAL"),
        /**
         * The Lt.
         */
        LT(" < ", "LITTLE THAN"),
        /**
         * The Le.
         */
        LE(" <= ", "LITTLE OR EQUAL"),
        /**
         * Li types.
         */
        LI(" ~ ", "LIKE"),
        /**
         * Bw types.
         */
        BW(" <> ", "BETWEEN"),
        /**
         * The Nl.
         */
        NL(" !~ ", "NOT LIKE");

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