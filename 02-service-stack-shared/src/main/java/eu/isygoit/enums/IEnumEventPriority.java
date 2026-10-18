// IEnumEventPriority.java
package eu.isygoit.enums;

public interface IEnumEventPriority {
    int STR_ENUM_SIZE = 10;

    enum Types implements IEnum {
        LOW("LOW"), NORMAL("NORMAL"), HIGH("HIGH"), URGENT("URGENT");
        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}