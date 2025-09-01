package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectMapperAndService;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.ICrudControllerUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.exception.*;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdAssignable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;

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
        M extends IIdAssignableDto<I> & IDto,
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
    public static final String CONTROLLER_SERVICE = "controller api";
    /**
     * The constant DEFAULT_PAGE_SIZE.
     */
    protected static final int DEFAULT_PAGE_SIZE = 50;
    /**
     * The constant MAX_PAGE_SIZE.
     */
    protected static final int MAX_PAGE_SIZE = 1000;
    /**
     * The constant CREATE_DATE_FIELD.
     */
    protected static final String CREATE_DATE_FIELD = "createDate";
    @Getter
    private final Class<F> fullDtoClass = (Class<F>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[3];
    @Getter
    private final Class<M> minDtoClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    private EntityMapper<T, F> fullEntityMapper;
    private EntityMapper<T, M> minEntityMapper;
    private S crudService;

    /**
     * Validates a bulk operation list.
     *
     * @param objects List to validate
     * @throws BadArgumentException if list is empty or exceeds max size
     */
    protected static void validateBulkOperation(List objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException("Bulk operation list cannot be empty or null");
        }
        if (objects.size() > MAX_PAGE_SIZE) {
            throw new BadArgumentException(
                    String.format("Bulk operation size %d exceeds maximum %d", objects.size(), MAX_PAGE_SIZE));
        }
    }

    @Override
    public final S crudService() throws BeanNotFoundException, ServiceNotDefinedException {
        if (this.crudService == null) {
            InjectMapperAndService injectMapperAndService = this.getClass().getAnnotation(InjectMapperAndService.class);
            if (injectMapperAndService != null) {
                this.crudService = getApplicationContextService().getBean((Class<S>) injectMapperAndService.service())
                        .orElseThrow(() -> new BeanNotFoundException(CONTROLLER_SERVICE));
            } else {
                InjectService injectService = this.getClass().getAnnotation(InjectService.class);
                if (injectService != null) {
                    this.crudService = getApplicationContextService().getBean((Class<S>) injectService.value())
                            .orElseThrow(() -> new BeanNotFoundException(CONTROLLER_SERVICE));
                }
            }
        }

        return this.crudService;
    }

    @Override
    public final EntityMapper<T, F> mapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.fullEntityMapper == null) {
            InjectMapperAndService injectMapperAndService = this.getClass().getAnnotation(InjectMapperAndService.class);
            if (injectMapperAndService != null) {
                this.fullEntityMapper = getApplicationContextService().getBean(injectMapperAndService.mapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + injectMapperAndService.mapper().getSimpleName()));
            } else {
                InjectMapper injectMapper = this.getClass().getAnnotation(InjectMapper.class);
                this.fullEntityMapper = getApplicationContextService().getBean(injectMapper.mapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + injectMapperAndService.mapper().getSimpleName()));
            }
        }

        return this.fullEntityMapper;
    }

    @Override
    public final EntityMapper<T, M> minDtoMapper() throws BeanNotFoundException, MapperNotDefinedException {
        if (this.minEntityMapper == null) {
            InjectMapperAndService injectMapperAndService = this.getClass().getAnnotation(InjectMapperAndService.class);
            if (injectMapperAndService != null) {
                this.minEntityMapper = getApplicationContextService().getBean(injectMapperAndService.minMapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + injectMapperAndService.mapper().getSimpleName()));
            } else {
                InjectMapper injectMapper = this.getClass().getAnnotation(InjectMapper.class);
                this.minEntityMapper = getApplicationContextService().getBean(injectMapper.minMapper())
                        .orElseThrow(() -> new BeanNotFoundException(ERROR_BEAN_NOT_FOUND + ": " + injectMapperAndService.mapper().getSimpleName()));
            }
        }

        return this.minEntityMapper;
    }
}
