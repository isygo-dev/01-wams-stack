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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

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
public abstract class CrudControllerUtils<I extends Serializable, E extends IIdEntity,
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
    private final Class<E> persistentClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    @Getter
    private final Class<M> minDtoClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    @Getter
    private final Class<F> fullDtoClass = (Class<F>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[3];
    private EntityMapper<E, F> fullEntityMapper;
    private EntityMapper<E, M> minEntityMapper;
    private S crudService;

    @Override
    public final S crudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (this.crudService == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.crudService = (S) getApplicationContextService().getBean(ctrlDef.service());
                if (this.crudService == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.service().getSimpleName());
                    throw new BeanNotFoundException(CONTROLLER_SERVICE);
                }
            } else {
                CtrlService ctrlService = this.getClass().getAnnotation(CtrlService.class);
                if (ctrlService != null) {
                    this.crudService = (S) getApplicationContextService().getBean(ctrlService.value());
                    if (this.crudService == null) {
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
    public final EntityMapper<E, F> mapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.fullEntityMapper == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlDef.mapper());
                if (this.fullEntityMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.mapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlDef.mapper().getSimpleName());
                }
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlMapper.mapper());
                if (this.fullEntityMapper == null) {
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
    public final EntityMapper<E, M> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.minEntityMapper == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.minEntityMapper = getApplicationContextService().getBean(ctrlDef.minMapper());
                if (this.minEntityMapper == null) {
                    log.error(ERROR_BEAN_NOT_FOUND, ctrlDef.minMapper().getSimpleName());
                    throw new BeanNotFoundException(ctrlDef.minMapper().getSimpleName());
                }
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.minEntityMapper = getApplicationContextService().getBean(ctrlMapper.minMapper());
                if (this.minEntityMapper == null) {
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
