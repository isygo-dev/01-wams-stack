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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * The type Abstract jwt auth filter.
 */
@Slf4j
public abstract class AbstractJwtAuthFilter extends OncePerRequestFilter {

    /**
     * The constant should_not_filter.
     */
    @Value("${app.feign.shouldNotFilterKey}")
    public static String should_not_filter = "$SHOULD_NOT_FILTER";

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
        //extract to list to be customisable
        return request.getRequestURI().startsWith("/api/v1/public")
                //|| request.getRequestURI().startsWith("/socket")
                || request.getRequestURI().contains("/image/download")
                || request.getRequestURI().contains("/file/download")
                || should_not_filter.equals(request.getHeader("SHOULD_NOT_FILTER_KEY"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = UrlHelper.getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            log.info("Request received:  {} - {} - {} ", request.getMethod(), request.getAuthType(), request.getRequestURI());
            try {
                String subject = jwtService.extractSubject(jwt);
                String userName = jwtService.extractUserName(jwt);
                String application = jwtService.extractApplication(jwt);
                String domain = jwtService.extractDomain(jwt);
                Boolean isAdmin = jwtService.extractIsAdmin(jwt);
                isTokenValid(jwt, domain, application, userName);

                SecurityContextHolder.getContext()
                        .setAuthentication(new CustomAuthentification(CustomUserDetails.builder()
                                .username(subject)
                                .isAdmin(isAdmin)
                                .password("password")
                                .passwordExpired(false)
                                //.authorities(Account.getAuthorities(account))
                                .domainEnabled(true)
                                .accountEnabled(true)
                                .accountExpired(true)
                                .accountLocked(true)
                                .build(),
                                "password",
                                new ArrayList<>()));

                addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                        .senderDomain(domain)
                        .senderUser(userName)
                        .isAdmin(isAdmin)
                        .logApp(application)
                        .build()));

                filterChain.doFilter(request, response);
            } catch (JwtException | IllegalArgumentException | TokenInvalidException e) {
                log.error("<Error>: Invalid token: {}> {} / {}", request.getMethod(), request.getRequestURI(), e);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        } else {
            log.error("<Error>: Missed token for request {}> {}", request.getMethod(), request.getRequestURI());
            addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                    .senderDomain(DomainConstants.SUPER_DOMAIN_NAME)
                    .senderUser("root")
                    .isAdmin(true)
                    .logApp("application")
                    .build()));
            /*
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Missing token");
             */
            filterChain.doFilter(request, response);
        }
    }
}
