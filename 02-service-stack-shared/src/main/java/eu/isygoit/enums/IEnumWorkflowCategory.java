package eu.isygoit.enums;

/**
 * The interface Enum workflow category.
 */
public interface IEnumWorkflowCategory {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Sequential types.
         */
//https://kissflow.com/workflow/what-is-a-workflow/#types
        SEQUENTIAL("Sequential"),
        /**
         * Parallel types.
         */
        PARALLEL("Parallel"),
        /**
         * Rules driven types.
         */
        RULES_DRIVEN("Rules-driven"),
        /**
         * The State machine.
         */
        STATE_MACHINE("State machine");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
