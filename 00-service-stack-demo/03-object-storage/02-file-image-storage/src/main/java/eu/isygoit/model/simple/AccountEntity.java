package eu.isygoit.model.simple;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
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
@Table(name = "ACCOUNT")
public class AccountEntity extends AuditableEntity<Long> implements ITenantAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_sequence_generator")
    @SequenceGenerator(name = "account_sequence_generator", sequenceName = "account_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Criteria
    @Column(name = "LOGIN", nullable = false, updatable = false, unique = true)
    private String login;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "PASS_KEY", nullable = false)
    private String passkey;
}