package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.MapperNotDefinedException;
import eu.isygoit.exception.ServiceNotDefinedException;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdEntity;

/**
 * The interface Crud controller utils.
 *
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
public interface ICrudControllerUtils<T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceUtils<T>> {

    /**
     * Mapper entity mapper.
     *
     * @return the entity mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<T, FULLD> mapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Min dto mapper entity mapper.
     *
     * @return the entity mapper
     * @throws BeanNotFoundException     the bean not found exception
     * @throws MapperNotDefinedException the mapper not defined exception
     */
    EntityMapper<T, MIND> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException;

    /**
     * Crud service s.
     *
     * @return the s
     * @throws BeanNotFoundException      the bean not found exception
     * @throws ServiceNotDefinedException the service not defined exception
     */
    S crudService() throws BeanNotFoundException, ServiceNotDefinedException;
}
