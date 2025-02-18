package eu.isygoit.model;

import java.util.List;

/**
 * The interface Multi file entity.
 *
 * @param <E> the type parameter
 */
public interface AssignableMultiFile<E extends LinkedFile> {

    /**
     * Gets additional files.
     *
     * @return the additional files
     */
    List<E> getAdditionalFiles();

    /**
     * Sets additional files.
     *
     * @param list the list
     */
    void setAdditionalFiles(List<E> list);
}
