package eu.isygoit.filter;

import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.enums.IEnumAppToken;
import eu.isygoit.exception.RemoteCallFailedException;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.token.TokenServiceApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;

/**
 * The type Jwt kms client auth filter.
 * <p>
 * Inherited services should autowire:
 * private TokenServiceApi tokenService;
 */
@Slf4j
public abstract class JwtKmsClientAuthFilter extends AbstractJwtAuthFilter {

    /**
     * Gets repository instance.
     *
     * @return the repository instance
     */
    protected abstract TokenServiceApi getTokenServiceInstance();

    /**
     * Gets token service.
     *
     * @return the token service
     */
    protected Optional<TokenServiceApi> getTokenService() {
        return Optional.ofNullable(getTokenServiceInstance());
    }

    @Override
    public boolean isTokenValid(String jwt, String domain, String application, String userName) {
        var tokenService = getTokenService()
                .orElseThrow(() -> {
                    log.error("Token service is unavailable for domain: {}, application: {}, user: {}", domain, application, userName);
                    return new TokenInvalidException("No token validator available");
                });

        try {
            var requestContext = RequestContextDto.builder().build();
            var result = tokenService.validateToken(requestContext, domain, application, IEnumAppToken.Types.ACCESS, jwt,
                    userName.toLowerCase() + "@" + domain);

            if (!result.getStatusCode().is2xxSuccessful() || !result.hasBody() || Boolean.FALSE.equals(result.getBody())) {
                log.error("Token validation failed for domain: {}, application: {}, user: {}", domain, application, userName);
                throw new TokenInvalidException("KMS::isTokenValid - Token invalid");
            }
            log.info("Token validated successfully for user: {} in domain: {}, application: {}", userName, domain, application);
        } catch (Exception e) {
            log.error("Remote Feign call failed for token validation: ", e);
            throw new RemoteCallFailedException("Error during token validation call", e);  // Custom exception
        }

        return true;
    }

    @Override
    public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            log.debug("No attributes to add to request");
            return;
        }

        attributes.forEach((key, value) -> {
            log.debug("Setting attribute: {} = {}", key, value);
            request.setAttribute(key, value);
        });
    }
}