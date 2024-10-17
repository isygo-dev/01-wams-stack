package eu.isygoit.config;

import eu.isygoit.enums.IEnumJwtStorage;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Jwt properties.
 */
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @Value("${app.jwt.secretKey}")
    private String secretKey;

    @Value("${app.jwt.signatureAlgorithm}")
    private SignatureAlgorithm signatureAlgorithm;

    @Value("${app.jwt.life-time-ms}")
    private Integer lifeTimeInMs;

    @Value("${app.jwt.storage-type}")
    private IEnumJwtStorage.Types jwtStorageType;
}
