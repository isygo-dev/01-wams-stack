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

/**
 * The type Application context service.
 */
@Slf4j
@Service
@Transactional
public class ApplicationContextServiceImpl implements ApplicationContextService {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public <T> T getBean(Class<T> beanClass) throws BeanNotFoundException {
        T bean = this.applicationContext.getBean(beanClass);
        if (bean == null) {
            log.error("<Error>: bean {} not found", beanClass.getSimpleName());
            throw new BeanNotFoundException(beanClass.getSimpleName());
        }

        return bean;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) throws BeansException {
        return applicationContext.getBeansWithAnnotation(annotationClass);
    }
}
