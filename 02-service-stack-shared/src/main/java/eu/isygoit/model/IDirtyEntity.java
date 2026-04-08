package eu.isygoit.model;

import java.util.Set;

/**
 * Marker interface for entities that support dirty-checking before an update.
 * <p>
 * When an entity implements this interface, the service layer will fetch the
 * persisted original and compare it field-by-field with the incoming object.
 * Only if at least one tracked field has changed will the update be allowed to
 * proceed; otherwise an {@code ObjectNotUpdatableException} is thrown.
 * <p>
 * Fields returned by {@link #ignoreFields()} are excluded from the comparison.
 * Typical candidates are audit / version fields that the persistence layer
 * manages automatically and that must never block a legitimate update:
 * <pre>{@code
 * @Override
 * public Set<String> ignoreFields() {
 *     return Set.of("createdDate", "updatedDate", "createdBy", "updatedBy", "version");
 * }
 * }</pre>
 */
public interface IDirtyEntity {

    /**
     * Returns the set of field names that must be excluded from the dirty check.
     * <p>
     * The comparison walks the entire class hierarchy of the entity; every field
     * whose name appears in this set is silently skipped. Returning an empty set
     * (or {@code null}) means every field participates in the check.
     *
     * @return a non-null (but possibly empty) set of field names to ignore
     */
    Set<String> ignoreFields();
}