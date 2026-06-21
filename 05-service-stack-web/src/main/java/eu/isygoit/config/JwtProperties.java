package eu.isygoit.config;

import eu.isygoit.enums.IEnumJwtStorage;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Jwt properties.
 */
@Getter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @Value("${app.jwt.secretKey:o9ZqX8kL2mNpR4tY7uVwB1cD3eF5gH6jK8lM0nP2rS4tU6vW8xY0z}")
    private String secretKey;

    @Value("${app.jwt.signatureAlgorithm:HS256}")
    private SignatureAlgorithm signatureAlgorithm;

    @Value("${app.jwt.life-time-ms:14400000}")
    private Integer lifeTimeInMs;

    @Value("${app.jwt.storage-type:LOCAL}")
    private IEnumJwtStorage.Types jwtStorageType;

    @Value("${app.jwt.all-audiences:*}")
    private String jwtAllAudiences;

    @Value("${app.jwt.prefix-audiences:KMS.}")
    private String jwtPrefixAudiences;
}
