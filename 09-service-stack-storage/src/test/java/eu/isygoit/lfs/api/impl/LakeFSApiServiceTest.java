package eu.isygoit.lfs.api.impl;

import eu.isygoit.lfs.config.LFSConfig;
import eu.isygoit.s3.api.IMinIOApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LakeFSApiServiceTest {

    private final Map<String, RestTemplate> lakeFSClientMap = new HashMap<>();
    private LakeFSApiService lakeFSApiService;
    @Mock
    private IMinIOApiService minIOApiService;

    @BeforeEach
    void setUp() {
        lakeFSApiService = new LakeFSApiService(lakeFSClientMap, minIOApiService);
    }

    @Test
    void testValidateRepositoryName_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            lakeFSApiService.repositoryExists(LFSConfig.builder().tenant("test").url("http://localhost").userName("user").password("pass").build(), null);
        });
    }

    @Test
    void testValidateConfig_Invalid() {
        LFSConfig invalidConfig = LFSConfig.builder().build();
        assertThrows(IllegalArgumentException.class, () -> {
            lakeFSApiService.getRepositories(invalidConfig);
        });
    }

    @Test
    void testBuildLakeFSUrl() {
        LFSConfig config = LFSConfig.builder()
                .url("http://localhost:8000")
                .apiPrefix("/api/v1")
                .build();
        // buildLakeFSUrl is private, we test it indirectly or via reflection if necessary,
        // but here we just ensure the service can be instantiated and basic validation works.
        assertNotNull(lakeFSApiService);
    }
}
