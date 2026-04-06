package eu.isygoit.model;

import eu.isygoit.model.json.JsonBasedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EVENTS")
public class EventEntity extends JsonBasedEntity<Long> implements ITenantAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "events_sequence_generator")
    @SequenceGenerator(name = "events_sequence_generator", sequenceName = "events_sequence", allocationSize = 1)
    @Setter
    private Long id;

    @Setter
    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;
}