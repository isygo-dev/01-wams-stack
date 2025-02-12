package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.MapperNotDefinedException;
import eu.isygoit.exception.ServiceNotDefinedException;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdEntity;

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
public interface ICrudControllerUtils<I extends Serializable, E extends IIdEntity,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceUtils<I, E>> {

    /**
     * Mapper entity mapper.
     *
     * @return the entity mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<E, F> mapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Min dto mapper entity mapper.
     *
     * @return the entity mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<E, M> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Crud service s.
     *
     * @return the s
     * @throws BeanNotFoundException      the bean not found exception
     * @throws ServiceNotDefinedException the service not defined exception
     */
    S crudService() throws BeanNotFoundException, ServiceNotDefinedException;
}
