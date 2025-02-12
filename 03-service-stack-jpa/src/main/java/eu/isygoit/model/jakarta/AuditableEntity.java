package eu.isygoit.model.jakarta;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Auditable entity.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<I extends Serializable> extends AbstractEntity<I> {

    @CreatedDate
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = ComSchemaColumnConstantName.C_CREATE_DATE, updatable = false)
    private Date createDate;

    @CreatedBy
    @Column(name = ComSchemaColumnConstantName.C_CREATED_BY, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = ComSchemaColumnConstantName.C_UPDATE_DATE)
    private Date updateDate;

    @LastModifiedBy
    @Column(name = ComSchemaColumnConstantName.C_UPDATED_BY)
    private String updatedBy;
}
