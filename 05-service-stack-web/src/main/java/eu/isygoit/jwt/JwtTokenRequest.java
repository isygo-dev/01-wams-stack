package eu.isygoit.jwt;

import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.Builder;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

/**
 * Request object for creating a JWT token.
 */
@Builder
public record JwtTokenRequest(
        String subject,
        Map<String, Object> claims,
        String issuer,
        List<String> audience,
        SecureDigestAlgorithm<?, ?> algorithm,
        String key,
        Integer lifeTimeInMs
) {
}
