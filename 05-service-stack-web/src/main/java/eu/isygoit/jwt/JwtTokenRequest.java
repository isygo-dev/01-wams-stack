package eu.isygoit.jwt;

import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.Builder;

import java.util.Map;
import java.util.Set;

/**
 * Request object for creating a JWT token.
 */
@Builder
public record JwtTokenRequest(
        String subject,
        Map<String, Object> claims,
        String issuer,
        Set<String> audience,
        SecureDigestAlgorithm<?, ?> algorithm,
        String key,
        Integer lifeTimeInMs
) {
}
