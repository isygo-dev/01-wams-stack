package eu.isygoit.storage.s3.service;

import eu.isygoit.storage.s3.api.impl.OxiCloudApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * The type Min io service.
 */
@Slf4j
@Service
public class OxiCloudService extends OxiCloudApiService {


    /**
     * Instantiates a new Oxi cloud api service.
     *
     * @param s3ClientMap the s 3 client map
     */
    @Autowired
    public OxiCloudService(Map<String, S3Client> s3ClientMap) {
        super(s3ClientMap);
    }
}
