package eu.isygoit.app;

import eu.isygoit.exception.BeanNotFoundException;
import org.springframework.beans.BeansException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * The interface Application context service.
 */
public interface ApplicationContextService {

    /**
     * Gets bean.
     *
     * @param <T>       the type parameter
     * @param beanClass the bean class
     * @return the bean
     * @throws BeanNotFoundException the bean not found exception
     */
    <T> T getBean(Class<T> beanClass) throws BeanNotFoundException;

    /**
     * Gets beans with annotation.
     *
     * @param var1 the var 1
     * @return the beans with annotation
     * @throws BeansException the beans exception
     */
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> var1) throws BeansException;
}
