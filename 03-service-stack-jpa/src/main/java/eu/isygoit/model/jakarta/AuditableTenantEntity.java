package eu.isygoit.model.jakarta;

import eu.isygoit.constants.ErrorCodeConstants;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.timeline.ITimelineEventSource;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@AllArgsConstructor
public abstract class AuditableTenantEntity<I extends Serializable>
        extends AuditableEntity<I>
        implements ITenantAssignable, IIdAssignable<I>, ITimelineEventSource {


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
