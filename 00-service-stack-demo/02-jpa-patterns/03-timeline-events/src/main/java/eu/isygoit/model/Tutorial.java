package eu.isygoit.model;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.annotation.TrackChanges;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.service.TimelineEventListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Tutorial.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TUTORIALS")
@EntityListeners(TimelineEventListener.class)
public class Tutorial extends AuditableEntity<Long> implements ITenantAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutorials_sequence_generator")
    @SequenceGenerator(name = "tutorials_sequence_generator", sequenceName = "tutorials_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @TrackChanges
    @Column(name = "TITLE")
    private String title;

    @TrackChanges
    @Column(name = "DESCRIPTION")
    private String description;

    @TrackChanges
    @Column(name = "PUBLISHED")
    private boolean published;
}
