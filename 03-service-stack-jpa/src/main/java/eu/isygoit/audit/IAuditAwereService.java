package eu.isygoit.audit;

import java.util.Optional;

/**
 * The interface Audit awere api.
 *
 * @param <T> the type parameter
 */
public interface IAuditAwereService<T> {

    /**
     * Gets current auditor.
     *
     * @return the current auditor
     */
    Optional<T> getCurrentAuditor();
}
