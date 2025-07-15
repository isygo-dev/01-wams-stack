package eu.isygoit.storage.lfs.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


/**
 * The type Lfs configuration.
 */
@Configuration
public class LFSConfiguration {

    /**
     * Lake fs client map map.
     *
     * @return the map
     */
    @Bean
    public Map<String, RestTemplate> lakeFSClientMap() {
        return new HashMap<>();
    }
}
