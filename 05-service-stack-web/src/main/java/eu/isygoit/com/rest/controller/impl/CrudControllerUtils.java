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
import java.util.Objects;

/**
 * The type Crud controller utils.
 *
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class CrudControllerUtils<T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceUtils<T>>
        extends ControllerExceptionHandler
        implements ICrudControllerUtils<T, MIND, FULLD, S> {

    /**
     * The constant ERROR_BEAN_NOT_FOUND.
     */
    public static final String ERROR_BEAN_NOT_FOUND = "<Error>: bean {} not found";
    /**
     * The constant CONTROLLER_SERVICE.
     */
    public static final String CONTROLLER_SERVICE = "controller service";
    @Getter
    private final Class<T> fullDtoClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    @Getter
    private final Class<T> minDtoClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    private EntityMapper<T, FULLD> fullEntityMapper;
    private EntityMapper<T, MIND> minEntityMapper;
    private S crudService;

    @Override
    public final S crudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (Objects.isNull(this.crudService)) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (Objects.nonNull(ctrlDef)) {
                this.crudService = (S) getApplicationContextService().getBean(ctrlDef.service());
                if (Objects.isNull(this.crudService)) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.service().getSimpleName());
                    throw new BeanNotFoundException(CONTROLLER_SERVICE);
                }
            } else {
                CtrlService ctrlService = this.getClass().getAnnotation(CtrlService.class);
                if (Objects.nonNull(ctrlService)) {
                    this.crudService = (S) getApplicationContextService().getBean(ctrlService.value());
                    if (Objects.isNull(this.crudService)) {
                        log.error(ERROR_BEAN_NOT_FOUND, ctrlService.value().getSimpleName());
                        throw new BeanNotFoundException(CONTROLLER_SERVICE);
                    }
                }
                log.error("<Error>: Service bean not defined");
                throw new ServiceNotDefinedException(CONTROLLER_SERVICE);
            }
        }

        return this.crudService;
    }

    @Override
    public final EntityMapper<T, FULLD> mapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (Objects.isNull(this.fullEntityMapper)) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (Objects.nonNull(ctrlDef)) {
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlDef.mapper());
                if (Objects.isNull(this.fullEntityMapper)) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.mapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlDef.mapper().getSimpleName());
                }
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlMapper.mapper());
                if (Objects.isNull(this.fullEntityMapper)) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlMapper.mapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlMapper.mapper().getSimpleName());
                } else {
                    log.error("<Error>: FullDto Mapper bean not defined");
                    throw new MapperNotDefinedException("Mapper");
                }
            }
        }

        return this.fullEntityMapper;
    }

    @Override
    public final EntityMapper<T, MIND> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (Objects.isNull(this.minEntityMapper)) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (Objects.nonNull(ctrlDef)) {
                this.minEntityMapper = getApplicationContextService().getBean(ctrlDef.minMapper());
                if (Objects.isNull(this.minEntityMapper)) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.minMapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlDef.minMapper().getSimpleName());
                }
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.minEntityMapper = getApplicationContextService().getBean(ctrlMapper.minMapper());
                if (Objects.isNull(this.minEntityMapper)) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlMapper.minMapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlMapper.minMapper().getSimpleName());
                } else {
                    log.error("<Error>: MinDto Mapper bean not defined");
                    throw new MapperNotDefinedException("MinDto Mapper");
                }
            }
        }

        return this.minEntityMapper;
    }
}
