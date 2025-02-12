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
            CtrlHandler ctrlHandler = this.getClass().getAnnotation(CtrlHandler.class);
            if (ctrlHandler != null) {
                this.exceptionHandler = applicationContextService.getBean(ctrlHandler.value());
                if (this.exceptionHandler == null) {
                    log.error("<Error>: Exception Handler bean not found");
                    throw new BeanNotFoundException(this.getClass().getSimpleName());
                }
            } else {
                CtrlDef ctrlDef = this.getClass().getAnnotation(CtrlDef.class);
                if (ctrlDef != null) {
                    this.exceptionHandler = applicationContextService.getBean(ctrlDef.handler());
                    if (this.exceptionHandler == null) {
                        log.error("<Error>: Exception Handler bean not found");
                        throw new BeanNotFoundException(this.getClass().getSimpleName());
                    }
                } else {
                    log.error("<Error>: Exception Handler bean not defined, please use CtrlExHandler or CtrlDef annotations");
                    throw new ExceptionHandlerNotDefinedException(this.getClass().getSimpleName());
                }
            }
        }

        return this.exceptionHandler;
    }

    @Override
    public String handleExceptionMessage(Throwable throwable) {
        if (exceptionHandler() != null) {
            return exceptionHandler().handleError(throwable);
        }
        return throwable.toString();
    }

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        log.error("<Error>: Exception {}", e);
        return ResponseFactory.ResponseInternalError(exceptionHandler().handleError(e));
    }
}
