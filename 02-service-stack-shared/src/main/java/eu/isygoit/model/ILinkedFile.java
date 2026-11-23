package eu.isygoit.model;


/**
 * The interface Linked file.
 */
public interface ILinkedFile extends IFileEntity {
    /**
     * Gets code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Sets code.
     *
     * @param code the code
     */
    void setCode(String code);

    /**
     * Sets the category of the file.
     *
     * @param category the category
     */
    void setCategory(String category);
}
