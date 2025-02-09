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
import eu.isygoit.model.IIdEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;

/**
 * The type Crud controller utils.
 *
 * @param <T>     the type parameter for entity type
 * @param <MIND>  the type parameter for minimal DTO
 * @param <FULLD> the type parameter for full DTO
 * @param <S>     the type parameter for service type
 */
@Slf4j
public abstract class CrudControllerUtils<T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceUtils<T>>
        extends ControllerExceptionHandler
        implements ICrudControllerUtils<T, MIND, FULLD, S> {

    public static final String ERROR_BEAN_NOT_FOUND = "<Error>: bean {} not found";
    public static final String CONTROLLER_SERVICE = "encrypt service";

    @Getter
    private final Class<T> fullDtoClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];

    @Getter
    private final Class<T> minDtoClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    private EntityMapper<T, FULLD> fullDtoMapper;
    private EntityMapper<T, MIND> minDtoMapper;
    private S serviceInstance;

    @Override
    public final S crudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (serviceInstance == null) {
            var controllerDef = getClass().getAnnotation(CtrlDef.class);

            if (controllerDef != null) {
                serviceInstance = (S) getApplicationContextService().getBean(controllerDef.service());
                if (serviceInstance == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, controllerDef.service().getSimpleName());
                    throw new BeanNotFoundException(CONTROLLER_SERVICE);
                }
            } else {
                var controllerService = getClass().getAnnotation(CtrlService.class);
                if (controllerService != null) {
                    serviceInstance = (S) getApplicationContextService().getBean(controllerService.value());
                    if (serviceInstance == null) {
                        log.error(ERROR_BEAN_NOT_FOUND, controllerService.value().getSimpleName());
                        throw new BeanNotFoundException(CONTROLLER_SERVICE);
                    }
                } else {
                    log.error("<Error>: Service bean not defined");
                    throw new ServiceNotDefinedException(CONTROLLER_SERVICE);
                }
            }
        }
        return serviceInstance;
    }

    @Override
    public final EntityMapper<T, FULLD> mapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (fullDtoMapper == null) {
            var controllerDef = getClass().getAnnotation(CtrlDef.class);

            if (controllerDef != null) {
                fullDtoMapper = getApplicationContextService().getBean(controllerDef.mapper());
                if (fullDtoMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, controllerDef.mapper().getSimpleName());
                    throw new BeanNotFoundException(controllerDef.mapper().getSimpleName());
                }
            } else {
                var controllerMapper = getClass().getAnnotation(CtrlMapper.class);
                fullDtoMapper = getApplicationContextService().getBean(controllerMapper.mapper());

                if (fullDtoMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, controllerMapper.mapper().getSimpleName());
                    throw new BeanNotFoundException(controllerMapper.mapper().getSimpleName());
                } else {
                    log.error("<Error>: FullDto Mapper bean not defined");
                    throw new MapperNotDefinedException("FullDto Mapper");
                }
            }
        }
        return fullDtoMapper;
    }

    @Override
    public final EntityMapper<T, MIND> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (minDtoMapper == null) {
            var controllerDef = getClass().getAnnotation(CtrlDef.class);

            if (controllerDef != null) {
                minDtoMapper = getApplicationContextService().getBean(controllerDef.minMapper());
                if (minDtoMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, controllerDef.minMapper().getSimpleName());
                    throw new BeanNotFoundException(controllerDef.minMapper().getSimpleName());
                }
            } else {
                var controllerMapper = getClass().getAnnotation(CtrlMapper.class);
                minDtoMapper = getApplicationContextService().getBean(controllerMapper.minMapper());

                if (minDtoMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, controllerMapper.minMapper().getSimpleName());
                    throw new BeanNotFoundException(controllerMapper.minMapper().getSimpleName());
                } else {
                    log.error("<Error>: MinDto Mapper bean not defined");
                    throw new MapperNotDefinedException("MinDto Mapper");
                }
            }
        }
        return minDtoMapper;
    }
}