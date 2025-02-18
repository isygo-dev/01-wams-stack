package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.CtrlDef;
import eu.isygoit.annotation.CtrlMapper;
import eu.isygoit.annotation.CtrlService;
import eu.isygoit.com.rest.controller.ICrudControllerUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.MapperNotDefinedException;
import eu.isygoit.exception.ServiceNotDefinedException;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.AssignableId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * The type Crud controller utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class CrudControllerUtils<I extends Serializable, E extends AssignableId,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceUtils<I, E>>
        extends ControllerExceptionHandler
        implements ICrudControllerUtils<I, E, M, F, S> {

    /**
     * The constant ERROR_BEAN_NOT_FOUND.
     */
    public static final String ERROR_BEAN_NOT_FOUND = "<Error>: bean {} not found";
    /**
     * The constant CONTROLLER_SERVICE.
     */
    public static final String CONTROLLER_SERVICE = "controller service";
    @Getter
    private final Class<E> persistentClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[1])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<E>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine persistent class"));

    @Getter
    private final Class<M> minDtoClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[2])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<M>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine min DTO class"));

    @Getter
    private final Class<F> fullDtoClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[3])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<F>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine full DTO class"));

    private EntityMapper<E, F> fullEntityMapper;
    private EntityMapper<E, M> minEntityMapper;
    private S crudService;

    @Override
    public final S getCrudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (this.crudService == null) {
            getCrudService(this.getClass().getAnnotation(CtrlDef.class));
        }

        return this.crudService;
    }

    private void getCrudService(CtrlDef ctrlDef) {
        Optional.ofNullable(ctrlDef)
                .map(CtrlDef::service)
                .map(serviceType -> getContextService().getBean(serviceType))
                .ifPresentOrElse(
                        bean -> this.crudService = (S) bean,
                        () -> getCrudService(this.getClass().getAnnotation(CtrlService.class))
                );
    }

    private void getCrudService(CtrlService ctrlService) {
        Optional.ofNullable(ctrlService)
                .map(CtrlService::value)
                .map(serviceType -> getContextService().getBean(serviceType))
                .ifPresentOrElse(
                        bean -> this.crudService = (S) bean,
                        () -> {
                            log.error(ERROR_BEAN_NOT_FOUND, ctrlService == null ? "Unknown" : ctrlService.value().getSimpleName());
                            throw new ServiceNotDefinedException(CONTROLLER_SERVICE);
                        }
                );
    }


    @Override
    public final EntityMapper<E, F> getMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.fullEntityMapper == null) {
            getMapper(this.getClass().getAnnotation(CtrlDef.class));
        }

        return this.fullEntityMapper;
    }

    private void getMapper(CtrlDef ctrlDef) {
        Optional.ofNullable(ctrlDef)
                .map(CtrlDef::mapper)
                .map(mapperType -> getContextService().getBean(mapperType))
                .ifPresentOrElse(
                        bean -> this.fullEntityMapper = (EntityMapper<E, F>) bean,
                        () -> getMapper(this.getClass().getAnnotation(CtrlMapper.class))
                );
    }

    private void getMapper(CtrlMapper ctrlMapper) {
        Optional.ofNullable(ctrlMapper)
                .map(CtrlMapper::mapper)
                .map(mapperType -> getContextService().getBean(mapperType))
                .ifPresentOrElse(
                        bean -> this.fullEntityMapper = (EntityMapper<E, F>) bean,
                        () -> {
                            log.error(ERROR_BEAN_NOT_FOUND, ctrlMapper == null ? "Unknown" : ctrlMapper.mapper().getSimpleName());
                            throw new MapperNotDefinedException("Mapper");
                        }
                );
    }


    @Override
    public final EntityMapper<E, M> getMinDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.minEntityMapper == null) {
            getMinDtoMapper(this.getClass().getAnnotation(CtrlDef.class));
        }

        return this.minEntityMapper;
    }

    private void getMinDtoMapper(CtrlDef ctrlDef) {
        Optional.ofNullable(ctrlDef)
                .map(CtrlDef::minMapper)
                .map(mapperType -> getContextService().getBean(mapperType))
                .ifPresentOrElse(
                        bean -> this.minEntityMapper = (EntityMapper<E, M>) bean,
                        () -> getMinDtoMapper(this.getClass().getAnnotation(CtrlMapper.class))
                );
    }

    private void getMinDtoMapper(CtrlMapper ctrlMapper) {
        Optional.ofNullable(ctrlMapper)
                .map(CtrlMapper::minMapper)
                .map(mapperType -> getContextService().getBean(mapperType))
                .ifPresentOrElse(
                        bean -> this.minEntityMapper = (EntityMapper<E, M>) bean,
                        () -> {
                            log.error(ERROR_BEAN_NOT_FOUND, ctrlMapper == null ? "Unknown" : ctrlMapper.minMapper().getSimpleName());
                            throw new MapperNotDefinedException("MinDto Mapper");
                        }
                );
    }

}
