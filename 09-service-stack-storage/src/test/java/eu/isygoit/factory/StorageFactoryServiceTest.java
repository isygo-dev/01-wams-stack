package eu.isygoit.factory;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.service.IObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageFactoryServiceTest {

    @Mock
    private BeanFactory beanFactory;

    @InjectMocks
    private StorageFactoryService storageFactoryService;

    @Mock
    private IObjectStorageService minioService;

    @Mock
    private IObjectStorageService lakefsService;

    @Test
    void testGetService_Minio() {
        // Arrange
        String beanName = "MinIOStorageService";
        when(beanFactory.getBean(beanName, IObjectStorageService.class)).thenReturn(minioService);

        // Act
        IObjectStorageService result = storageFactoryService.getService(IEnumStorage.Types.MINIO_STORAGE);

        // Assert
        assertNotNull(result);
        assertEquals(minioService, result);
        verify(beanFactory, times(1)).getBean(beanName, IObjectStorageService.class);
    }

    @Test
    void testGetService_LakeFS() {
        // Arrange
        String beanName = "LakeFSStorageService";
        when(beanFactory.getBean(beanName, IObjectStorageService.class)).thenReturn(lakefsService);

        // Act
        IObjectStorageService result = storageFactoryService.getService(IEnumStorage.Types.LAKEFS_STORAGE);

        // Assert
        assertNotNull(result);
        assertEquals(lakefsService, result);
        verify(beanFactory, times(1)).getBean(beanName, IObjectStorageService.class);
    }
}
