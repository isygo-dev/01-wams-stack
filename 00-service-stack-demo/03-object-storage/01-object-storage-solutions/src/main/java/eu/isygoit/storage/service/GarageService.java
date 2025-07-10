package eu.isygoit.storage.service;

import eu.isygoit.storage.api.impl.GarageApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Garage service.
 */
@Slf4j
@Service
public class GarageService extends GarageApiService {


    /**
     * Instantiates a new Garage service.
     *
     * @param s3ClientMap the s 3 client map
     */
    public GarageService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}
