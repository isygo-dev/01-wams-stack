package eu.isygoit.jwt.interceptor;

import eu.isygoit.jwt.JwtService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class SmartAuthInterceptor implements RequestInterceptor {

    private final String serviceApiKey;
    private final String serviceId;

    public SmartAuthInterceptor(String serviceApiKey, String serviceId) {
        this.serviceApiKey = serviceApiKey;
        this.serviceId = serviceId;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // Case 1: No HTTP context (pure internal call)
        if (requestAttributes == null || requestAttributes.getRequest() == null) {
            log.debug("Pure internal call - using API Key");
            applyApiKeyAuth(requestTemplate, serviceId, serviceApiKey);
            return;
        }

        HttpServletRequest request = requestAttributes.getRequest();

        // Case 2: Check for JWT
        String jwtToken = request.getHeader(JwtService.AUTHORIZATION);
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            log.debug("JWT found - forwarding...");
            requestTemplate.header(JwtService.AUTHORIZATION, jwtToken);
            return;
        }

        // Case 3: Check for existing API Key
        String apiKey = request.getHeader(JwtService.X_API_KEY);
        if (apiKey != null && !apiKey.isEmpty()) {
            log.debug("API Key found - forwarding...");

            String req_serviceId = request.getHeader(JwtService.X_SERVICE_ID);
            applyApiKeyAuth(requestTemplate, req_serviceId != null ? req_serviceId : "unknown-external", serviceApiKey);
            return;
        }

        // Case 5: NO JWT and NO API Key - Handle based on configuration
        requestTemplate.header(JwtService.X_AUTH_WARNING, "No authentication provided");
    }

    private void applyApiKeyAuth(RequestTemplate requestTemplate, String serviceId, String serviceApiKey) {
        if (serviceApiKey != null && !serviceApiKey.isEmpty()) {
            requestTemplate.header(JwtService.X_API_KEY, serviceApiKey);
            requestTemplate.header(JwtService.X_SERVICE_ID, serviceId);
            requestTemplate.header(JwtService.X_INTERNAL_CALL, "true");
            requestTemplate.header(JwtService.AUTHORIZATION, serviceApiKey);
            log.debug("Applied API Key: {} for service: {}",
                    maskApiKey(serviceApiKey), serviceId);
        } else {
            log.error("Service API Key not configured - cannot authenticate internal call");
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null) return null;
        if (apiKey.length() <= 8) return "***";
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
