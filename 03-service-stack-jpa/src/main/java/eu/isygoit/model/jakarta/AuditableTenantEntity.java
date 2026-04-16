package eu.isygoit.model.jakarta;

import eu.isygoit.constants.ErrorCodeConstants;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.timeline.ITimelineEventSource;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class AuditableTenantEntity<I extends Serializable>
        extends AuditableEntity<I>
        implements ITenantAssignable, IIdAssignable<I>, ITimelineEventSource {

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Override
    public String resolveTenant() {
        return getTenant();
    }

    @Override
    public String resolveModifiedBy() {
        return getUpdatedBy() != null ? getUpdatedBy()
                : getCreatedBy() != null ? getCreatedBy()
                : ErrorCodeConstants.UNKNOWN;
    }

    @Override
    public String resolveElementId() {
        return getId() != null ? getId().toString() : null;
    }
}
