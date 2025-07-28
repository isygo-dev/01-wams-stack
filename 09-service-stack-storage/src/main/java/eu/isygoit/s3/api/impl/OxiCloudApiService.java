package eu.isygoit.s3.api.impl;

import eu.isygoit.s3.api.IOxiCloudApiService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Oxi cloud api service.
 */
@Slf4j
public abstract class OxiCloudApiService extends S3BucketApiService implements IOxiCloudApiService {

    /**
     * Instantiates a new Oxi cloud api service.
     *
     * @param s3ClientMap the s 3 client map
     */
    public OxiCloudApiService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}