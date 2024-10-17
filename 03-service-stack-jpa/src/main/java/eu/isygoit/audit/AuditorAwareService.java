package eu.isygoit.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The type Auditor aware service.
 */
@Slf4j
@Service
@Transactional
public class AuditorAwareService implements IAuditAwereService<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("anonymousUser");
        }

        Object principal = authentication.getPrincipal();
        if (UserDetails.class.isAssignableFrom(principal.getClass())) {
            return Optional.of(((UserDetails) authentication.getPrincipal()).getUsername());
        } else if (String.class.isAssignableFrom(principal.getClass())) {
            return Optional.of((String) authentication.getPrincipal());
        }

        return Optional.of("anonymousUser");
    }
}
