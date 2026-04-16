package eu.isygoit.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * The type Auditor aware.
 */
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private IAuditorAwareService auditorAwareService;

    /**
     * Instantiates a new Auditor aware.
     *
     * @param auditorAwareService the audit aware api
     */
    public AuditorAwareImpl(IAuditorAwareService auditorAwareService) {
        this.auditorAwareService = auditorAwareService;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return auditorAwareService.getCurrentAuditor();
    }
}
