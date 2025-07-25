package eu.isygoit.storage.s3.service;

import eu.isygoit.storage.s3.api.impl.MinIOApiService;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The type Min io service.
 */
@Slf4j
@Profile("MinIO")
@Service
public class MinIOService extends MinIOApiService {

    /**
     * Instantiates a new Min io service.
     *
     * @param minIoMap the min io map
     */
    @Autowired
    public MinIOService(Map<String, MinioClient> minIoMap) {
        super(minIoMap);
    }
}
