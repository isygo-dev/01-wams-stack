package eu.isygoit.app.impl;

import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.exception.BeanNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the ApplicationContextService interface that provides methods
 * for retrieving beans from the application context.
 */
@Slf4j
@Service
@Transactional
public class ApplicationContextServiceImpl implements ApplicationContextService {

    private final ApplicationContext applicationContext;

    @Autowired
    public ApplicationContextServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Retrieves a bean from the Spring ApplicationContext by its class type.
     *
     * @param beanClass the class type of the bean to retrieve
     * @param <E>       the type of the bean
     * @return the bean instance of the specified class
     * @throws BeanNotFoundException if no bean is found for the given class type
     */
    @Override
    public <E> E getBean(Class<E> beanClass) throws BeanNotFoundException {
        // Using Optional to avoid null checks
        var bean = Optional.ofNullable(applicationContext.getBean(beanClass))
                .orElseThrow(() -> {
                    String errorMsg = String.format("Bean of type '%s' could not be found in the application context.", beanClass.getSimpleName());
                    log.error(errorMsg);
                    return new BeanNotFoundException(beanClass.getSimpleName());
                });

        String successMsg = String.format("Successfully retrieved bean of type '%s' from the application context.", beanClass.getSimpleName());
        log.info(successMsg);
        return bean;
    }

    /**
     * Retrieves all beans that are annotated with a given annotation.
     *
     * @param annotationClass the annotation to search for on beans
     * @return a Map of bean names and bean instances with the given annotation
     * @throws BeansException if any error occurs while retrieving the beans
     */
    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) throws BeansException {
        // Using stream to filter beans with the specified annotation and logging the results
        var beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationClass).entrySet().stream()
                .peek(entry -> {
                    String msg = String.format("Found bean '%s' of type '%s' with annotation '%s'.",
                            entry.getKey(), entry.getValue().getClass().getSimpleName(), annotationClass.getSimpleName());
                    log.debug(msg);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (beansWithAnnotation.isEmpty()) {
            log.warn("No beans found in the application context with annotation '{}'.", annotationClass.getSimpleName());
        } else {
            log.info("{} beans found with annotation '{}'.", beansWithAnnotation.size(), annotationClass.getSimpleName());
        }

        return beansWithAnnotation;
    }
}