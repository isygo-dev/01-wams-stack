package eu.isygoit.model;

import java.util.List;

/**
 * The interface File entity.
 */
public interface IFileEntity {

    /**
     * Gets type.
     *
     * @return the type
     */
    String getType();

    /**
     * Sets type.
     *
     * @param type the type
     */
    void setType(String type);

    /**
     * Gets file name.
     *
     * @return the file name
     */
    String getFileName();

    /**
     * Sets file name.
     *
     * @param fileName the file name
     */
    void setFileName(String fileName);

    /**
     * Gets original file name.
     *
     * @return the original file name
     */
    String getOriginalFileName();

    /**
     * Sets original file name.
     *
     * @param originalFileName the original file name
     */
    void setOriginalFileName(String originalFileName);

    /**
     * Gets path.
     *
     * @return the path
     */
    String getPath();

    /**
     * Sets path.
     *
     * @param path the path
     */
    void setPath(String path);

    /**
     * Gets extension.
     *
     * @return the extension
     */
    String getExtension();

    /**
     * Sets extension.
     *
     * @param extension the extension
     */
    void setExtension(String extension);

    /**
     * Gets tags.
     *
     * @return the tags
     */
    List<String> getTags();
}
