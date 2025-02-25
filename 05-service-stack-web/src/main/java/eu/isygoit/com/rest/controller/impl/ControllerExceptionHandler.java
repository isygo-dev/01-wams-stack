package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.CtrlDef;
import eu.isygoit.annotation.CtrlHandler;
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
                            Optional.ofNullable(this.getClass().getAnnotation(CtrlHandler.class)).map(CtrlHandler::value),
                            Optional.ofNullable(this.getClass().getAnnotation(CtrlDef.class)).map(CtrlDef::handler)
                    ).flatMap(Optional::stream)
                    .findFirst()
                    .orElseThrow(() -> new ExceptionHandlerNotDefinedException(this.getClass().getSimpleName()));

            // Récupération du bean correspondant
            this.exceptionHandler = Optional.ofNullable(applicationContextService.getBean(handlerClass))
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
        return ResponseFactory.ResponseInternalError(exceptionHandler().handleError(e));
    }
}
