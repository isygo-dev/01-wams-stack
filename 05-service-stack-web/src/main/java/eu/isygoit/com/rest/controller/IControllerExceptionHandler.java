package eu.isygoit.com.rest.controller;

import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.ExceptionHandlerNotDefinedException;
import eu.isygoit.exception.handler.IExceptionHandler;
import org.springframework.http.ResponseEntity;

/**
 * The interface Controller exception handler.
 */
public interface IControllerExceptionHandler {

    /**
     * Exception handler exception handler.
     *
     * @return the exception handler
     * @throws BeanNotFoundException               the bean not found exception
     * @throws ExceptionHandlerNotDefinedException the exception handler not defined exception
     */
    IExceptionHandler exceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException;

    /**
     * Gets back exception response.
     *
     * @param e the e
     * @return the back exception response
     */
    ResponseEntity getBackExceptionResponse(Throwable e);

    /**
     * Handle exception message string.
     *
     * @param throwable the throwable
     * @return the string
     */
    String handleExceptionMessage(Throwable throwable);
}
