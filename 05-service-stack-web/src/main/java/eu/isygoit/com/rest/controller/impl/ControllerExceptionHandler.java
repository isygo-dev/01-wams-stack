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

import java.util.Objects;

/**
 * Abstract base class for handling exceptions in controllers.
 * Provides exception handling mechanisms by utilizing the application context and annotations.
 */
@Slf4j
public abstract class ControllerExceptionHandler implements IControllerExceptionHandler {

    // Service for fetching beans from the Spring application context
    @Getter
    @Autowired
    private ApplicationContextService applicationContextService;

    // The handler responsible for processing exceptions
    private IExceptionHandler handler;

    /**
     * Retrieves the appropriate exception handler for the controller.
     *
     * This method checks for annotations on the class to determine the handler to use.
     * It first checks for a CtrlHandler annotation, then a CtrlDef annotation,
     * and throws an error if neither is found or if the handler bean is not defined.
     *
     * @return The exception handler instance
     * @throws BeanNotFoundException if the exception handler bean is not found in the context
     * @throws ExceptionHandlerNotDefinedException if no handler is defined via annotations
     */
    public final IExceptionHandler getExceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException {
        if (Objects.isNull(this.handler)) {
            CtrlHandler ctrlHandlerAnnotation = this.getClass().getAnnotation(CtrlHandler.class);
            if (Objects.nonNull(ctrlHandlerAnnotation)) {
                // Fetch the handler bean from the application context
                this.handler = applicationContextService.getBean(ctrlHandlerAnnotation.value());
                if (Objects.isNull(this.handler)) {
                    log.error("Exception Handler bean not found for class: {}", this.getClass().getSimpleName());
                    throw new BeanNotFoundException(this.getClass().getSimpleName());
                }
            } else {
                // If CtrlHandler annotation is absent, check for CtrlDef annotation
                CtrlDef ctrlDefAnnotation = this.getClass().getAnnotation(CtrlDef.class);
                if (Objects.nonNull(ctrlDefAnnotation)) {
                    this.handler = applicationContextService.getBean(ctrlDefAnnotation.handler());
                    if (Objects.isNull(this.handler)) {
                        log.error("Exception Handler bean not found for handler defined in CtrlDef annotation for class: {}", this.getClass().getSimpleName());
                        throw new BeanNotFoundException(this.getClass().getSimpleName());
                    }
                } else {
                    // Neither annotation is present, cannot proceed
                    log.error("No Exception Handler bean defined. Please use CtrlExHandler or CtrlDef annotations on class: {}", this.getClass().getSimpleName());
                    throw new ExceptionHandlerNotDefinedException(this.getClass().getSimpleName());
                }
            }
        }

        return this.handler;
    }

    /**
     * Handles the exception message, using the exception handler to format the error.
     *
     * @param throwable The throwable to handle
     * @return The formatted error message
     */
    @Override
    public String handleExceptionMessage(Throwable throwable) {
        if (Objects.nonNull(getExceptionHandler())) {
            // Use the exception handler to generate a detailed error message
            return getExceptionHandler().handleError(throwable);
        }
        // Fallback to the default throwable string representation if no handler is available
        return throwable.toString();
    }

    /**
     * Generates a response for an internal error, including the exception message.
     * Logs the exception before returning a standardized response.
     *
     * @param e The exception to handle
     * @return A ResponseEntity with the internal error message
     */
    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        // Log the exception with detailed information
        log.error("An internal error occurred: {}", e.getMessage(), e);  // Added exception stack trace in log
        // Return a standardized error response
        return ResponseFactory.ResponseInternalError(getExceptionHandler().handleError(e));
    }
}