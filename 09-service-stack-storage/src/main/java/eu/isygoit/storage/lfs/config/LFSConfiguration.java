package eu.isygoit.storage.lfs.config;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.Map;


/**
 * The type Minio configuration.
 */
@Configuration
public class LFSConfiguration {

    /**
     * Min io map map.
     *
     * @return the map
     */
    @Bean
    public Map<String, RestTemplate> lakeFSClientMap() {
        return new HashMap<>();
    }
}
