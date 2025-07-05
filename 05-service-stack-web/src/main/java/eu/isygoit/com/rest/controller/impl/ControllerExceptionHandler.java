package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.InjectExceptionHandler;
import eu.isygoit.annotation.InjectMapperAndService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.controller.IControllerExceptionHandler;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.ExceptionHandlerNotDefinedException;
import eu.isygoit.exception.handler.IExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The type Controller exception handler.
 */
@Slf4j
public abstract class ControllerExceptionHandler implements IControllerExceptionHandler {

    @Getter
    @Autowired
    private ApplicationContextService applicationContextService;

    private IExceptionHandler exceptionHandler;

    public final IExceptionHandler exceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException {
        if (this.exceptionHandler == null) {
            // Récupération de la classe du handler via les annotations
            var handlerClass = Stream.of(
                            Optional.ofNullable(this.getClass().getAnnotation(InjectExceptionHandler.class)).map(InjectExceptionHandler::value),
                            Optional.ofNullable(this.getClass().getAnnotation(InjectMapperAndService.class)).map(InjectMapperAndService::handler)
                    ).flatMap(Optional::stream)
                    .findFirst()
                    .orElseThrow(() -> new ExceptionHandlerNotDefinedException(this.getClass().getSimpleName()));

            // Récupération du bean correspondant
            this.exceptionHandler = applicationContextService.getBean(handlerClass)
                    .orElseThrow(() -> new BeanNotFoundException(this.getClass().getSimpleName()));
        }

        return this.exceptionHandler;
    }


    @Override
    public String handleExceptionMessage(Throwable throwable) {
        return Optional.ofNullable(exceptionHandler())
                .map(handler -> handler.handleError(throwable))
                .orElseGet(throwable::toString);
    }

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        log.error("<Error>: Exception {}", e);
        try {
            return ResponseFactory.responseInternalServerError(exceptionHandler().handleError(e));
        } catch (ExceptionHandlerNotDefinedException ex) {
            return ResponseFactory.responseInternalServerError(e.getMessage());
        }
    }
}
