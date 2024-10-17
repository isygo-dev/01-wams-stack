package eu.isygoit.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.EnableCassandraAuditing;
import org.springframework.data.domain.AuditorAware;

/**
 * The type Cassandra config.
 */
@Configuration
@EnableCassandraAuditing(auditorAwareRef = "cassandra_auditorAware")
public class CassandraConfig {

    @Autowired
    private IAuditAwereService<String> auditAwereService;

    /**
     * Auditor aware auditor aware.
     *
     * @return the auditor aware
     */
    @Bean(name = "cassandra_auditorAware")
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl(auditAwereService);
    }
}
