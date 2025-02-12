package eu.isygoit;

import eu.isygoit.api.IApiExtractor;
import eu.isygoit.app.ApplicationContextService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

/**
 * The type Service starter.
 */
@Slf4j
public class ServiceStarter {

    /**
     * The constant ERROR_EXTRACT_API_FAILS.
     */
    public static final String ERROR_EXTRACT_API_FAILS = "<Error>: Extract api fails {}";

    @Autowired
    private ApplicationContextService applicationContextService;
    @Autowired
    private IApiExtractor apiExtractor;

    /**
     * Extract apis.
     */
    @EventListener(ApplicationReadyEvent.class)
    public final void extractApis() {
        //Extract controller apis to build permission list
        applicationContextService.getBeansWithAnnotation(RestController.class).values().forEach(ctrl -> {
            try {
                if (apiExtractor != null) {
                    apiExtractor.extractApis(ctrl.getClass());
                } else {
                    log.warn("<Warning>: Extract service is null {}", ctrl.getClass().getSimpleName());
                }
            } catch (InvocationTargetException e) {
                log.error(ERROR_EXTRACT_API_FAILS, ctrl.getClass().getSimpleName(), e);
            } catch (NoSuchMethodException e) {
                log.error(ERROR_EXTRACT_API_FAILS, ctrl.getClass().getSimpleName(), e);
            } catch (InstantiationException e) {
                log.error(ERROR_EXTRACT_API_FAILS, ctrl.getClass().getSimpleName(), e);
            } catch (IllegalAccessException e) {
                log.error(ERROR_EXTRACT_API_FAILS, ctrl.getClass().getSimpleName(), e);
            }
        });
    }
}
