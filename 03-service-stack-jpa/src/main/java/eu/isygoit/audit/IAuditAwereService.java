package eu.isygoit.audit;

import java.util.Optional;

/**
 * The interface Audit awere service.
 *
 * @param <E> the type parameter
 */
public interface IAuditAwereService<E> {

    /**
     * Gets current auditor.
     *
     * @return the current auditor
     */
    Optional<E> getCurrentAuditor();
}
