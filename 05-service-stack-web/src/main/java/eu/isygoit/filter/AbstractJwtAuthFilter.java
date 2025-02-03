package eu.isygoit.filter;

import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.helper.UrlHelper;
import eu.isygoit.jwt.IJwtService;
import eu.isygoit.security.CustomAuthentification;
import eu.isygoit.security.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * The type Abstract jwt auth filter.
 */
@Slf4j
public abstract class AbstractJwtAuthFilter extends OncePerRequestFilter {

    @Value("${app.feign.shouldNotFilterKey}")
    private String shouldNotFilter;

    @Autowired
    private IJwtService jwtService;

    /**
     * Is token valid boolean.
     *
     * @param jwt         the jwt
     * @param domain      the domain
     * @param application the application
     * @param userName    the user name
     * @return the boolean
     */
    public abstract boolean isTokenValid(String jwt, String domain, String application, String userName);

    /**
     * Add attributes.
     *
     * @param request    the request
     * @param attributes the attributes
     */
    public abstract void addAttributes(HttpServletRequest request, Map<String, Object> attributes);

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.info("Jwt auth filter: attribute SHOULD_NOT_FILTER_KEY: {}", request.getHeader("SHOULD_NOT_FILTER_KEY"));
        return isExcludedUri(request) || shouldNotFilter.equals(request.getHeader("SHOULD_NOT_FILTER_KEY"));
    }

    private boolean isExcludedUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/public")
                || uri.contains("/image/download")
                || uri.contains("/file/download");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = UrlHelper.getJwtFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            log.info("Request received:  {} - {} - {}", request.getMethod(), request.getAuthType(), request.getRequestURI());
            try {
                handleJwtAuthentication(jwt, request);
                filterChain.doFilter(request, response);
            } catch (JwtException | IllegalArgumentException | TokenInvalidException e) {
                log.error("<Error>: Invalid token: {} > {} / {}", request.getMethod(), request.getRequestURI(), e);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        } else {
            log.error("<Error>: Missed token for request {} > {}", request.getMethod(), request.getRequestURI());
            setDefaultAttributes(request);
            filterChain.doFilter(request, response);
        }
    }

    private void handleJwtAuthentication(String jwt, HttpServletRequest request) {
        Optional<String> subject = jwtService.extractSubject(jwt);
        Optional<String> userName = jwtService.extractUserName(jwt);
        Optional<String> application = jwtService.extractApplication(jwt);
        Optional<String> domain = jwtService.extractDomain(jwt);
        Optional<Boolean> isAdmin = jwtService.extractIsAdmin(jwt);

        subject.ifPresentOrElse(value -> {
            userName.ifPresentOrElse(name -> {
                        if (!isTokenValid(jwt, domain.orElse("NA"), application.orElse("NA"), name)) {
                            throw new TokenInvalidException("Invalid JWT/userName");
                        }
                    },
                    () -> {
                        throw new TokenInvalidException("Invalid JWT/userName");
                    });

            setAuthentication(value, isAdmin.orElse(Boolean.FALSE));
        }, () -> {
            throw new TokenInvalidException("Invalid JWT/subject");
        });

        addAttributes(request, createRequestAttributes(domain, userName, isAdmin.orElse(Boolean.FALSE), application));
    }

    private void setAuthentication(String subject, Boolean isAdmin) {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .username(subject)
                .isAdmin(isAdmin)
                .password("password")
                .passwordExpired(false)
                .domainEnabled(true)
                .accountEnabled(true)
                .accountExpired(true)
                .accountLocked(true)
                .build();

        Authentication authentication = new CustomAuthentification(userDetails, "password", new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Map<String, Object> createRequestAttributes(Optional<String> domain, Optional<String> userName,
                                                        Boolean isAdmin, Optional<String> application) {
        return Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                .senderDomain(domain.orElse("NA"))
                .senderUser(userName.orElse("NA"))
                .isAdmin(isAdmin)
                .logApp(application.orElse("NA"))
                .build());
    }

    private void setDefaultAttributes(HttpServletRequest request) {
        addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                .senderDomain(DomainConstants.SUPER_DOMAIN_NAME)
                .senderUser("root")
                .isAdmin(true)
                .logApp("application")
                .build()));
    }
}