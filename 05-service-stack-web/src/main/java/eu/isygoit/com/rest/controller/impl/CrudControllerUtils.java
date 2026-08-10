package eu.isygoit.com.rest.controller.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectMapperAndService;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.annotation.RestConfiguration;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.controller.IControllerExceptionHandler;
import eu.isygoit.com.rest.controller.ICrudControllerUtils;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.exception.*;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdAssignable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        extends ControllerUtils
        implements ICrudControllerUtils<I, T, M, F, S>, IControllerExceptionHandler {

    @Getter
    private final Class<F> fullDtoClass = (Class<F>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[3];
    @Getter
    private final Class<M> minDtoClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    @Autowired
    private ControllerExceptionHandler controllerExceptionHandler;
    private EntityMapper<T, F> fullEntityMapper;
    private EntityMapper<T, M> minEntityMapper;
    private S crudService;

    private static final Cache<Class<?>, Class<?>> serviceClassCache = Caffeine.newBuilder().build();
    private static final Cache<Class<?>, Class<?>> mapperClassCache = Caffeine.newBuilder().build();
    private static final Cache<Class<?>, Class<?>> minMapperClassCache = Caffeine.newBuilder().build();

    /**
     * Validates a bulk operation list.
     *
     * @param objects List to validate
     * @throws BadArgumentException if list is empty or exceeds max size
     */
    protected static void validateBulkOperation(List<?> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException("Bulk operation list cannot be empty or null");
        }
        if (objects.size() > CtrlConstants.MAX_PAGE_SIZE) {
            throw new BadArgumentException(
                    String.format("Bulk operation size %d exceeds maximum %d", objects.size(), CtrlConstants.MAX_PAGE_SIZE));
        }
    }

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        return controllerExceptionHandler.getBackExceptionResponse(this.getClass(), e);
    }

    @Override
    public String handleExceptionMessage(Throwable throwable) {
        return controllerExceptionHandler.handleExceptionMessage(this.getClass(), throwable);
    }

    @Override
    public final S crudService() {
        if (this.crudService == null) {
            Class<S> serviceClass = (Class<S>) serviceClassCache.get(this.getClass(), clazz -> {
                RestConfiguration restConfiguration = clazz.getAnnotation(RestConfiguration.class);
                if (restConfiguration != null && restConfiguration.service() != ICrudServiceUtils.class) {
                    return restConfiguration.service();
                }

                InjectMapperAndService injectMapperAndService = clazz.getAnnotation(InjectMapperAndService.class);
                if (injectMapperAndService != null) {
                    return injectMapperAndService.service();
                }
                InjectService injectService = clazz.getAnnotation(InjectService.class);
                if (injectService != null) {
                    return injectService.value();
                }
                throw new ServiceNotDefinedException("Service not defined for " + clazz.getSimpleName());
            });

            ApplicationContextService contextService = getApplicationContextService();
            if (contextService == null) {
                log.warn("ApplicationContextService not found for controller {}. ControllerExceptionHandler might not be injected.", this.getClass().getSimpleName());
                // Fallback or rethrow if strictness is required.
                // For now rethrowing as it was effectively doing NPE before, but with better message.
                throw new BeanNotFoundException("ApplicationContextService not found (ControllerExceptionHandler might not be injected)");
            }

            this.crudService = contextService.getBean(serviceClass)
                    .orElseThrow(() -> new BeanNotFoundException(CtrlConstants.CONTROLLER_SERVICE));
        }

        return this.crudService;
    }

    @Override
    public final EntityMapper<T, F> mapper() {
        if (this.fullEntityMapper == null) {
            Class<EntityMapper<T, F>> mapperClass = (Class<EntityMapper<T, F>>) mapperClassCache.get(this.getClass(), clazz -> {
                RestConfiguration restConfiguration = clazz.getAnnotation(RestConfiguration.class);
                if (restConfiguration != null && restConfiguration.mapper() != EntityMapper.class) {
                    return restConfiguration.mapper();
                }

                InjectMapperAndService injectMapperAndService = clazz.getAnnotation(InjectMapperAndService.class);
                if (injectMapperAndService != null) {
                    return injectMapperAndService.mapper();
                }
                InjectMapper injectMapper = clazz.getAnnotation(InjectMapper.class);
                if (injectMapper != null) {
                    return injectMapper.mapper();
                }
                throw new MapperNotDefinedException("Full entity mapper for " + clazz.getSimpleName());
            });

            ApplicationContextService contextService = getApplicationContextService();
            if (contextService == null) {
                throw new BeanNotFoundException("ApplicationContextService not found (ControllerExceptionHandler might not be injected)");
            }

            this.fullEntityMapper = contextService.getBean(mapperClass)
                    .orElseThrow(() -> new BeanNotFoundException(CtrlConstants.ERROR_BEAN_NOT_FOUND + ": " + mapperClass.getSimpleName()));
        }

        return this.fullEntityMapper;
    }

    @Override
    public final EntityMapper<T, M> minDtoMapper() {
        if (this.minEntityMapper == null) {
            Class<EntityMapper<T, M>> mapperClass = (Class<EntityMapper<T, M>>) minMapperClassCache.get(this.getClass(), clazz -> {
                RestConfiguration restConfiguration = clazz.getAnnotation(RestConfiguration.class);
                if (restConfiguration != null && restConfiguration.minMapper() != EntityMapper.class) {
                    return restConfiguration.minMapper();
                }

                InjectMapperAndService injectMapperAndService = clazz.getAnnotation(InjectMapperAndService.class);
                if (injectMapperAndService != null) {
                    return injectMapperAndService.minMapper();
                }
                InjectMapper injectMapper = clazz.getAnnotation(InjectMapper.class);
                if (injectMapper != null) {
                    return injectMapper.minMapper();
                }
                throw new MapperNotDefinedException("Min entity mapper for " + clazz.getSimpleName());
            });

            ApplicationContextService contextService = getApplicationContextService();
            if (contextService == null) {
                throw new BeanNotFoundException("ApplicationContextService not found (ControllerExceptionHandler might not be injected)");
            }

            this.minEntityMapper = contextService.getBean(mapperClass)
                    .orElseThrow(() -> new BeanNotFoundException(CtrlConstants.ERROR_BEAN_NOT_FOUND + ": " + mapperClass.getSimpleName()));
        }

        return this.minEntityMapper;
    }
}
