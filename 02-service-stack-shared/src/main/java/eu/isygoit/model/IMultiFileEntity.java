package eu.isygoit.model;

import java.util.List;

/**
 * The interface Multi file entity.
 *
 * @param <T> the type parameter
 */
public interface IMultiFileEntity<T extends ILinkedFile> {

    /**
     * Gets additional files.
     *
     * @return the additional files
     */
    List<T> getAdditionalFiles();

    /**
     * Sets additional files.
     *
     * @param list the list
     */
    void setAdditionalFiles(List<T> list);
}
