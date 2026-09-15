package eu.isygoit.mapper;


import java.util.List;
import java.util.Set;

/**
 * The interface Entity mapper.
 *
 * @param <T> the type parameter
 * @param <MinD> the type parameter
 * @param <FullD> the type parameter
 */
public interface EntityMinFullMapper<T, MinD, FullD extends MinD> extends EntityMapper<T, FullD>{

    /**
     * Dto to entity t.
     *
     * @param object the object
     * @return the t
     */
    T dtoMinToEntity(MinD object);

    /**
     * Entity to dto d.
     *
     * @param object the object
     * @return the d
     */
    MinD entityToDtoMin(T object);

    /**
     * List dto to entity list.
     *
     * @param list the list
     * @return the list
     */
    List<T> listDtoMinToEntity(List<MinD> list);

    /**
     * List dto to entity set.
     *
     * @param list the list
     * @return the set
     */
    Set<T> listDtoMinToEntity(Set<MinD> list);

    /**
     * List entity to dto list.
     *
     * @param list the list
     * @return the list
     */
    List<MinD> listEntityToDtoMin(List<T> list);

    /**
     * List entity to dto set.
     *
     * @param list the list
     * @return the set
     */
    Set<MinD> listEntityToDtoMin(Set<T> list);
}
