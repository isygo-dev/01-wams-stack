package eu.isygoit.audit;

import java.util.Optional;

/**
 * The interface Audit aware api.
 *
 * @param <T> the type parameter
 */
public interface IAuditorAwareService<T> {

    /**
     * Gets current auditor.
     *
     * @return the current auditor
     */
    Optional<T> getCurrentAuditor();
}
