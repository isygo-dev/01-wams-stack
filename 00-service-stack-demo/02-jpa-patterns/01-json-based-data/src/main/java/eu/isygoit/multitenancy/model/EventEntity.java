package eu.isygoit.multitenancy.model;

import eu.isygoit.multitenancy.common.JsonBasedEntity;
import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EVENTS")
public class EventEntity extends JsonBasedEntity<Long> implements ITenantAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "events_seq_generator")
    @SequenceGenerator(name = "events_seq_generator", sequenceName = "events_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Column(name = "ELEMENT_TYPE", nullable = false)
    private String elementType;
}