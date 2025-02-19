package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.MapperNotDefinedException;
import eu.isygoit.exception.ServiceNotDefinedException;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.AssignableId;

import java.io.Serializable;

/**
 * The interface Crud controller utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
public interface ICrudControllerUtils<E extends AssignableId,
        I extends Serializable,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceUtils<E, I>> {

    /**
     * Gets mapper.
     *
     * @return the mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<E, F> getMapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Gets min dto mapper.
     *
     * @return the min dto mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<E, M> getMinDtoMapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Gets crud service.
     *
     * @return the crud service
     * @throws BeanNotFoundException      the bean not found exception
     * @throws ServiceNotDefinedException the service not defined exception
     */
    S getCrudService() throws BeanNotFoundException, ServiceNotDefinedException;
}
