package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.InjectExceptionHandler;
import eu.isygoit.annotation.InjectMapperAndService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.controller.IControllerExceptionHandler;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.ExceptionHandlerNotDefinedException;
import eu.isygoit.exception.ManagedException;
import eu.isygoit.exception.handler.IExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The type Controller exception handler.
 */
@Slf4j
@Component
public class ControllerExceptionHandler implements IControllerExceptionHandler {

    // Cache to store the determined IExceptionHandler for each controller class
    private final Map<Class<?>, IExceptionHandler> handlerCache = new ConcurrentHashMap<>();
    @Getter
    @Autowired
    protected ApplicationContextService applicationContextService;

    public final IExceptionHandler exceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException {
        return exceptionHandler(this.getClass());
    }

    public final IExceptionHandler exceptionHandler(Class<?> controllerClass) throws BeanNotFoundException, ExceptionHandlerNotDefinedException {
        return handlerCache.computeIfAbsent(controllerClass, (clazz) -> {
            // Récupération de la classe du handler via les annotations
            var handlerClass = Stream.of(
                            Optional.ofNullable(clazz.getAnnotation(InjectExceptionHandler.class)).map(InjectExceptionHandler::value),
                            Optional.ofNullable(clazz.getAnnotation(InjectMapperAndService.class)).map(InjectMapperAndService::handler)
                    ).flatMap(Optional::stream)
                    .findFirst()
                    .orElseThrow(() -> new ExceptionHandlerNotDefinedException(clazz.getSimpleName()));

            // Récupération du bean correspondant
            try {
                return (IExceptionHandler) applicationContextService.getBean((Class) handlerClass)
                        .orElseThrow(() -> new BeanNotFoundException(clazz.getSimpleName()));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public String handleExceptionMessage(Throwable throwable) {
        return handleExceptionMessage(this.getClass(), throwable);
    }

    public String handleExceptionMessage(Class<?> controllerClass, Throwable throwable) {
        try {
            return Optional.ofNullable(exceptionHandler(controllerClass))
                    .map(handler -> handler.handleError(throwable))
                    .orElseGet(throwable::toString);
        } catch (Exception e) {
            log.warn("Failed to determine exception handler for {}: {}", controllerClass.getSimpleName(), e.getMessage());
            return throwable.toString();
        }
    }

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        return getBackExceptionResponse(this.getClass(), e);
    }

    public ResponseEntity getBackExceptionResponse(Class<?> controllerClass, Throwable e) {
        log.error("<Error>: Exception {}", e);
        try {
            if (e instanceof ManagedException managedException) {
                return new ResponseEntity<>(e.getLocalizedMessage(), managedException.getHttpStatus());
            } else {
                IExceptionHandler handler = exceptionHandler(controllerClass);
                return ResponseFactory.responseInternalServerError(handler.handleError(e));
            }
        } catch (Exception ex) {
            log.warn("Error while building exception response for {}: {}", controllerClass.getSimpleName(), ex.getMessage());
            return ResponseFactory.responseInternalServerError(e.getLocalizedMessage());
        }
    }
}
