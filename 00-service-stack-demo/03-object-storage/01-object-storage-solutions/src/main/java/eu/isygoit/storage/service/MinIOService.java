package eu.isygoit.storage.service;

import eu.isygoit.storage.api.impl.MinIOApiService;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The type Min io service.
 */
@Slf4j
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
