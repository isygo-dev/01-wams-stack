package eu.isygoit.mapper;


import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.model.IIdEntity;

import java.util.List;
import java.util.Set;

/**
 * The interface Entity mapper.
 *
 * @param <T> the type parameter
 * @param <D> the type parameter
 */
public interface EntityMapper<T extends IIdEntity, D extends IIdentifiableDto> {

    /**
     * Dto to entity t.
     *
     * @param object the object
     * @return the t
     */
    T dtoToEntity(D object);

    /**
     * Entity to dto d.
     *
     * @param object the object
     * @return the d
     */
    D entityToDto(T object);

    /**
     * List dto to entity list.
     *
     * @param list the list
     * @return the list
     */
    List<T> listDtoToEntity(List<D> list);

    /**
     * List dto to entity set.
     *
     * @param list the list
     * @return the set
     */
    Set<T> listDtoToEntity(Set<D> list);

    /**
     * List entity to dto list.
     *
     * @param list the list
     * @return the list
     */
    List<D> listEntityToDto(List<T> list);

    /**
     * List entity to dto set.
     *
     * @param list the list
     * @return the set
     */
    Set<D> listEntityToDto(Set<T> list);

}
