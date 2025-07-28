package eu.isygoit.s3.api.impl;

import eu.isygoit.s3.api.ICephApiService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Ceph api service.
 */
@Slf4j
public abstract class CephApiService extends S3BucketApiService implements ICephApiService {

    /**
     * Instantiates a new Ceph api service.
     *
     * @param s3ClientMap the s 3 client map
     */
    public CephApiService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}