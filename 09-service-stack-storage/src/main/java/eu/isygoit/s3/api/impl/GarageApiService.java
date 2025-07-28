package eu.isygoit.s3.api.impl;

import eu.isygoit.s3.api.IGarageApiService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Garage api service.
 */
@Slf4j
public abstract class GarageApiService extends S3BucketApiService implements IGarageApiService {

    /**
     * Instantiates a new Garage api service.
     *
     * @param s3ClientMap the s 3 client map
     */
    public GarageApiService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}