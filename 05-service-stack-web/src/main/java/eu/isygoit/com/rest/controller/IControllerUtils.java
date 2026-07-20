package eu.isygoit.com.rest.controller;

import eu.isygoit.exception.RequestContextServiceNotDefinedException;
import eu.isygoit.service.RequestContextService;

/**
 * The interface Crud controller utils.
 */
public interface IControllerUtils {

    RequestContextService requestContextService() throws RequestContextServiceNotDefinedException;
}
