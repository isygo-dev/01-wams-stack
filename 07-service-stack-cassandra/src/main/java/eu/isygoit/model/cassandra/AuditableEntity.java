package eu.isygoit.model.cassandra;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;

import java.util.Date;

/**
 * The type Auditable entity.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class AuditableEntity<T> extends AbstractEntity<T> {

    @CreatedDate
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @Column(ComSchemaColumnConstantName.C_CREATE_DATE)
    private Date createDate;

    @CreatedBy
    @CassandraType(type = CassandraType.Name.TEXT)
    @Column(ComSchemaColumnConstantName.C_CREATED_BY)
    private String createdBy;

    @LastModifiedDate
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @Column(ComSchemaColumnConstantName.C_UPDATE_DATE)
    private Date updateDate;

    @LastModifiedBy
    @CassandraType(type = CassandraType.Name.TEXT)
    @Column(ComSchemaColumnConstantName.C_UPDATED_BY)
    private String updatedBy;
}
