package eu.isygoit.storage.lfs.service;

import eu.isygoit.storage.lfs.api.impl.LakeFSApiService;
import eu.isygoit.storage.s3.api.IMinIOApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * The type Min io service.
 */
@Slf4j
@Service
public class LakeFSService extends LakeFSApiService {


    /**
     * Instantiates a new Lake fs api service.
     *
     * @param lakeFSClientMap the lake fs client map
     * @param minIOApiService
     */
    public LakeFSService(Map<String, RestTemplate> lakeFSClientMap, IMinIOApiService minIOApiService) {
        super(lakeFSClientMap, minIOApiService);
    }
}
