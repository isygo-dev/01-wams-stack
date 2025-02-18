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
     * Gets exception handler.
     *
     * @return the exception handler
     * @throws BeanNotFoundException               the bean not found exception
     * @throws ExceptionHandlerNotDefinedException the exception handler not defined exception
     */
    IExceptionHandler getExceptionHandler() throws BeanNotFoundException, ExceptionHandlerNotDefinedException;

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
