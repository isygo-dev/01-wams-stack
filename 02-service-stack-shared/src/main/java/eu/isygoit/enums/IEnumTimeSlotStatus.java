package eu.isygoit.enums;


public interface IEnumTimeSlotStatus {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 10;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        SCHEDULED("SCHEDULED"),

        CANCELLED("CANCELLED"),

        POSTPONED("POSTPONED");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
