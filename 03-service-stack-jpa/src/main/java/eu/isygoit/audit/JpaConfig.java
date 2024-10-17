package eu.isygoit.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * The type Jpa config.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "jpa_auditorAware")
public class JpaConfig {

    @Autowired
    private IAuditAwereService<String> auditAwereService;

    /**
     * Auditor aware auditor aware.
     *
     * @return the auditor aware
     */
    @Bean(name = "jpa_auditorAware")
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl(auditAwereService);
    }
}
