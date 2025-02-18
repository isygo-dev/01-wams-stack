package eu.isygoit.mapper;


import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.model.AssignableId;

import java.util.List;
import java.util.Set;

/**
 * The interface Entity mapper.
 *
 * @param <E> the type parameter
 * @param <D> the type parameter
 */
public interface EntityMapper<E extends AssignableId, D extends IIdentifiableDto> {

    /**
     * Dto to entity t.
     *
     * @param object the object
     * @return the t
     */
    E dtoToEntity(D object);

    /**
     * Entity to dto d.
     *
     * @param object the object
     * @return the d
     */
    D entityToDto(E object);

    /**
     * List dto to entity list.
     *
     * @param list the list
     * @return the list
     */
    List<E> listDtoToEntity(List<D> list);

    /**
     * List dto to entity set.
     *
     * @param list the list
     * @return the set
     */
    Set<E> listDtoToEntity(Set<D> list);

    /**
     * List entity to dto list.
     *
     * @param list the list
     * @return the list
     */
    List<D> listEntityToDto(List<E> list);

    /**
     * List entity to dto set.
     *
     * @param list the list
     * @return the set
     */
    Set<D> listEntityToDto(Set<E> list);

}
