package eu.isygoit.model.jakarta;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The type Auditable cancelable entity.
 *
 * @param <T> the type parameter
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableCancelableEntity<T extends Serializable> extends CancelableEntity<T> {

    @CreatedDate
    @Column(name = ComSchemaColumnConstantName.C_CREATE_DATE, updatable = false)
    private LocalDateTime createDate;

    @CreatedBy
    @Column(name = ComSchemaColumnConstantName.C_CREATED_BY, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = ComSchemaColumnConstantName.C_UPDATE_DATE)
    private LocalDateTime updateDate;

    @LastModifiedBy
    @Column(name = ComSchemaColumnConstantName.C_UPDATED_BY)
    private String updatedBy;
}
