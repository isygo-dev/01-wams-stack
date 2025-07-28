package eu.isygoit.s3.config;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.Map;


/**
 * The type S 3 configuration.
 */
@Configuration
public class S3Configuration {

    /**
     * Min io map map.
     *
     * @return the map
     */
    @Bean
    public Map<String, MinioClient> minIoMap() {
        return new HashMap<>();
    }

    /**
     * S 3 client map map.
     *
     * @return the map
     */
    @Bean
    public Map<String, S3Client> s3ClientMap() {
        return new HashMap<>();
    }
}
