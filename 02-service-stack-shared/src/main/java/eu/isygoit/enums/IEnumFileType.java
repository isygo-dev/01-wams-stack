package eu.isygoit.enums;

import java.io.Serializable;

/**
 * The interface Enum file type.
 *
 * @param <T> the type parameter
 */
public interface IEnumFileType<T> extends Serializable {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {

        /**
         * Text types.
         */
        TEXT("TEXT"),
        /**
         * Html types.
         */
        HTML("HTML"),
        /**
         * Doc types.
         */
        DOC("DOC"),
        /**
         * Docx types.
         */
        DOCX("DOCX"),
        /**
         * Pdf types.
         */
        PDF("PDF");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
