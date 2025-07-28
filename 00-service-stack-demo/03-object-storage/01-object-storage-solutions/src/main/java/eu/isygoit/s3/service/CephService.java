package eu.isygoit.s3.service;

import eu.isygoit.s3.api.impl.CephApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Ceph service.
 */
@Slf4j
@Profile("Ceph")
@Service
public class CephService extends CephApiService {


    /**
     * Instantiates a new Ceph service.
     *
     * @param s3ClientMap the s 3 client map
     */
    @Autowired
    public CephService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}
