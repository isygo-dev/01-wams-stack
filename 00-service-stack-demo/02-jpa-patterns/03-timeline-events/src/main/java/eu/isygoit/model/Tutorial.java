package eu.isygoit.model;

import eu.isygoit.annotation.Criteria;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutorials_seq_generator")
    @SequenceGenerator(name = "tutorials_seq_generator", sequenceName = "tutorials_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Criteria
    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHED")
    private boolean published;
}
