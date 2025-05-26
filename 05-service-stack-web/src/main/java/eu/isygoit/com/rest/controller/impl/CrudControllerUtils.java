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
import eu.isygoit.model.IIdAssignable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;

/**
 * The type Crud controller utils.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class CrudControllerUtils<I, T extends IIdAssignable<I>,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceUtils<I, T>>
        extends ControllerExceptionHandler
        implements ICrudControllerUtils<I, T, M, F, S> {

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
    private EntityMapper<T, F> fullEntityMapper;
    private EntityMapper<T, M> minEntityMapper;
    private S crudService;

    @Override
    public final S crudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (this.crudService == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.crudService = getApplicationContextService().getBean((Class<S>) ctrlDef.service())
                        .orElseThrow(() -> new BeanNotFoundException(CONTROLLER_SERVICE));
            } else {
                CtrlService ctrlService = this.getClass().getAnnotation(CtrlService.class);
                if (ctrlService != null) {
                    this.crudService = getApplicationContextService().getBean((Class<S>) ctrlService.value())
                            .orElseThrow(() -> new BeanNotFoundException(CONTROLLER_SERVICE));
                }
                log.error("<Error>: Service bean not defined");
                throw new ServiceNotDefinedException(CONTROLLER_SERVICE);
            }
        }

        return this.crudService;
    }

    @Override
    public final EntityMapper<T, F> mapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.fullEntityMapper == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlDef.mapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + ctrlDef.mapper().getSimpleName()));
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.fullEntityMapper = getApplicationContextService().getBean(ctrlMapper.mapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + ctrlDef.mapper().getSimpleName()));
            }
        }

        return this.fullEntityMapper;
    }

    @Override
    public final EntityMapper<T, M> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.minEntityMapper == null) {
            CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
            if (ctrlDef != null) {
                this.minEntityMapper = getApplicationContextService().getBean(ctrlDef.minMapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + ctrlDef.mapper().getSimpleName()));
            } else {
                CtrlMapper ctrlMapper = this.getClass().getAnnotation(CtrlMapper.class);
                this.minEntityMapper = getApplicationContextService().getBean(ctrlMapper.minMapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + ctrlDef.mapper().getSimpleName()));
            }
        }

        return this.minEntityMapper;
    }
}
