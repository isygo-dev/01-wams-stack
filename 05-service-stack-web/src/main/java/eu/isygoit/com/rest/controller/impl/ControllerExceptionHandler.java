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

/**
 * The type Controller exception handler.
 */
@Slf4j
public abstract class ControllerExceptionHandler implements IControllerExceptionHandler {

    @Getter
    @Autowired
    private ApplicationContextService contextService;

    private IExceptionHandler exceptionHandler;

    public final IExceptionHandler getExceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException {
        if (this.exceptionHandler == null) {
            getExceptionHandler(this.getClass().getAnnotation(CtrlHandler.class));
        }

        return this.exceptionHandler;
    }

    private void getExceptionHandler(CtrlHandler ctrlHandler) {
        Optional.ofNullable(ctrlHandler)
                .map(CtrlHandler::value)
                .map(beanType -> getContextService().getBean(beanType))
                .ifPresentOrElse(
                        bean -> this.exceptionHandler = (IExceptionHandler) bean,
                        () -> getExceptionHandler(this.getClass().getAnnotation(CtrlDef.class))
                );
    }

    private void getExceptionHandler(CtrlDef ctrlDef) {
        Optional.ofNullable(ctrlDef)
                .map(CtrlDef::handler)
                .map(handlerType -> getContextService().getBean(handlerType))
                .ifPresentOrElse(
                        bean -> this.exceptionHandler = (IExceptionHandler) bean,
                        () -> {
                            log.error("<Error>: Exception Handler bean not defined, please use CtrlExHandler or CtrlDef annotations");
                            throw new ExceptionHandlerNotDefinedException(this.getClass().getSimpleName());
                        }
                );
    }

    @Override
    public String handleExceptionMessage(Throwable throwable) {
        if (getExceptionHandler() != null) {
            return getExceptionHandler().handleError(throwable);
        }
        return throwable.toString();
    }

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        log.error("<Error>: Exception {}", e);
        return ResponseFactory.ResponseInternalError(getExceptionHandler().handleError(e));
    }
}
