package eu.isygoit.helper;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * The type Spring class scanner.
 */
@Service
@Transactional
public class SpringClassScanner {

    /**
     * Find annotated classes set.
     *
     * @param annotationClass the annotation class
     * @param scanPackage     the scan package
     * @return the set
     */
    public Set<BeanDefinition> findAnnotatedClasses(Class<? extends Annotation> annotationClass, String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = createComponentScanner(annotationClass);
        return provider.findCandidateComponents(scanPackage);
    }

    private ClassPathScanningCandidateComponentProvider createComponentScanner(Class<? extends Annotation> annotationClass) {
        ClassPathScanningCandidateComponentProvider provider
                = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        return provider;
    }
}
