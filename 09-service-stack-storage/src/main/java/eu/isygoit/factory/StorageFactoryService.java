package eu.isygoit.factory;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.service.IObjectStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Storage factory service.
 */
@Slf4j
@Service
@Transactional
public class StorageFactoryService {

    private static final String SERVICE_NAME_SUFFIX = "StorageService";
    private final BeanFactory beanFactory;

    /**
     * Instantiates a new Storage factory service.
     *
     * @param beanFactory the bean factory
     */
    @Autowired
    public StorageFactoryService(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Gets service.
     *
     * @param type the type
     * @return the service
     */
    public IObjectStorageService getService(IEnumStorage.Types type) {
        return beanFactory.getBean(getServiceBeanName(type.meaning()), IObjectStorageService.class);
    }

    private String getServiceBeanName(String type) {
        return type + SERVICE_NAME_SUFFIX;
    }

}
