package eu.isygoit.jwt;

import io.jsonwebtoken.security.MacAlgorithm;
import lombok.Builder;

import java.util.Map;

/**
 * Request object for creating a JWT token.
 */
@Builder
public record JwtTokenRequest(
        String subject,
        Map<String, Object> claims,
        String issuer,
        String audience,
        MacAlgorithm algorithm,
        String key,
        Integer lifeTimeInMs
) {
}
