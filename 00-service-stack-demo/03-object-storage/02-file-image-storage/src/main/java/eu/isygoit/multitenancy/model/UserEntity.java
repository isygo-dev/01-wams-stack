package eu.isygoit.multitenancy.model;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USER_DETAILS")
public class UserEntity extends AuditableEntity<Long> implements ITenantAssignable, IImageEntity, ICodeAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_generator")
    @SequenceGenerator(name = "user_seq_generator", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false)
    private String tenant;

    //ICodeAssignable fields (should implement setCode & getCode)
    @Column(name = "CODE", nullable = false)
    private String code;

    @Criteria
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Criteria
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Builder.Default
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = Boolean.FALSE;

    //IImageEntity fields (should implement setImagePath & getImagePath)
    @Column(name = "IMAGE_PATH")
    private String imagePath;
}