package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.controller.IControllerExceptionHandler;
import eu.isygoit.com.rest.controller.IControllerUtils;
import eu.isygoit.exception.RequestContextServiceNotDefinedException;
import eu.isygoit.service.RequestContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * The type Crud controller utils.
 */
@Slf4j
public abstract class ControllerUtils
        extends ControllerExceptionHandler
        implements IControllerUtils, IControllerExceptionHandler {

    @Autowired
    private ControllerExceptionHandler controllerExceptionHandler;

    private RequestContextService requestContextService;

    @Override
    public ResponseEntity getBackExceptionResponse(Throwable e) {
        return controllerExceptionHandler.getBackExceptionResponse(this.getClass(), e);
    }

    @Override
    public String handleExceptionMessage(Throwable throwable) {
        return controllerExceptionHandler.handleExceptionMessage(this.getClass(), throwable);
    }

    public final ApplicationContextService getApplicationContextService() {
        return controllerExceptionHandler.getApplicationContextService();
    }

    @Override
    public RequestContextService requestContextService() throws RequestContextServiceNotDefinedException {
        if (requestContextService == null) {
            requestContextService = getApplicationContextService().getBean(RequestContextService.class)
                    .orElseThrow(() -> new RequestContextServiceNotDefinedException("RequestContextService not found"));
        }
        return requestContextService;
    }
}
