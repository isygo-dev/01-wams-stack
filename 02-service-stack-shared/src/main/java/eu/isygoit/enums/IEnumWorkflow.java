package eu.isygoit.enums;

/**
 * The interface Enum workflow.
 */
public interface IEnumWorkflow {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 7;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Project types.
         */
//https://kissflow.com/workflow/what-is-a-workflow/#types
        PROJECT("PROJECT"),
        /**
         * Process types.
         */
        PROCESS("PROCESS"),
        /**
         * Case types.
         */
        CASE("CASE");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
