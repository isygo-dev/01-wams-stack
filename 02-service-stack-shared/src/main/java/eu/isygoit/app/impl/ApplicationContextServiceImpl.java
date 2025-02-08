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
 * Application context service implementation.
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

    @Override
    public <T> T getBean(Class<T> beanClass) throws BeanNotFoundException {
        return Optional.ofNullable(applicationContext.getBean(beanClass))
                .orElseThrow(() -> {
                    log.error("Bean {} not found", beanClass.getSimpleName());
                    return new BeanNotFoundException(beanClass.getSimpleName());
                });
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) throws BeansException {
        return applicationContext.getBeansWithAnnotation(annotationClass);
    }
}
