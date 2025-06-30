package eu.isygoit.multitenancy.model;

import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.multitenancy.common.TenantEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TUTORIALS")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "TENANT_ID = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class Tutorial extends AuditableEntity<Long> implements ITenantAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tutorial_seq")
    @SequenceGenerator(name = "tutorial_seq", sequenceName = "TUTORIALS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHED")
    private boolean published;
}
