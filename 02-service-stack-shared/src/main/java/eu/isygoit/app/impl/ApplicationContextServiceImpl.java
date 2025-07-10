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

/**
 * The type Application context api.
 */
@Slf4j
@Service
@Transactional
public class ApplicationContextServiceImpl implements ApplicationContextService {


    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new Application context api.
     *
     * @param applicationContext the application context
     */
    public ApplicationContextServiceImpl(@Autowired ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> Optional<T> getBean(Class<T> beanClass) throws BeanNotFoundException {
        try {
            return Optional.ofNullable(this.applicationContext.getBean(beanClass));
        } catch (BeansException e) {
            log.error("<Error>: bean {} not found", beanClass.getSimpleName(), e);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) throws BeansException {
        return applicationContext.getBeansWithAnnotation(annotationClass);
    }
}
