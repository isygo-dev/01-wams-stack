package eu.isygoit.model;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.annotation.ElementType;
import eu.isygoit.annotation.JsonEntity;
import eu.isygoit.model.jakarta.AbstractEntity;
import eu.isygoit.model.json.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents an order placement event stored as a JSONB payload in the events table.
 *
 * <p>The {@link ElementType} annotation declares the stable discriminator key written
 * to the {@code element_type} column. Keeping this value stable across class renames
 * ensures existing rows are never orphaned.
 *
 * <p>Note: {@link ITenantAssignable} is implemented here because the shared service
 * contract requires it, but the tenant value is owned by the backing {@link EventEntity}
 * JPA row — not stored inside the JSONB payload. See issue #3 in the review for a
 * discussion of removing this interface from JSON element types.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonEntity(EventEntity.class)
@ElementType("ORDER_PLACED")
public class OrderPlacedEntity extends AbstractEntity<UUID>
        implements JsonElement<UUID>, ITenantAssignable {

    @Criteria
    private UUID id;

    @Criteria
    private String orderId;

    @Criteria
    private String customerId;

    @Criteria
    private BigDecimal amount;

    /**
     * @deprecated Tenant is stored on the JPA entity ({@link EventEntity}), not in the
     *             JSONB payload. This method returns an empty string as a structural
     *             placeholder. Use the tenant-aware service methods to scope operations
     *             by tenant.
     */
    @Deprecated(since = "review-point-3")
    @Override
    public String getTenant() {
        return "";
    }

    /**
     * @deprecated No-op. The tenant is set on the JPA entity by the service layer.
     */
    @Deprecated(since = "review-point-3")
    @Override
    public void setTenant(String tenant) {
        // Tenant is set on EventEntity by the service — not on the JSON payload.
    }
}