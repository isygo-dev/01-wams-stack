package eu.isygoit.storage.s3.factory;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.storage.s3.service.IObjectStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Storage factory api.
 */
@Slf4j
@Service
@Transactional
public class StorageFactoryService {

    private static final String SERVICE_NAME_SUFFIX = "StorageService";
    private final BeanFactory beanFactory;

    /**
     * Instantiates a new Storage factory api.
     *
     * @param beanFactory the bean factory
     */
    @Autowired
    public StorageFactoryService(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Gets api.
     *
     * @param type the type
     * @return the api
     */
    public IObjectStorageService getService(IEnumStorage.Types type) {
        return beanFactory.getBean(getServiceBeanName(type.meaning()), IObjectStorageService.class);
    }

    private String getServiceBeanName(String type) {
        return type + SERVICE_NAME_SUFFIX;
    }

}
