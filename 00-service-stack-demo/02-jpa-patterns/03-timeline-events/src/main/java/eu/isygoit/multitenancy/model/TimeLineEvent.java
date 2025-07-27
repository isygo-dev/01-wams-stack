package eu.isygoit.multitenancy.model;

import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Time line event.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TIME_LINE")
public class TimeLineEvent extends TimelineEventEntity implements ITenantAssignable {

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;
}
