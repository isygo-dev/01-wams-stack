package eu.isygoit.model.cassandra;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;

import java.util.Date;

/**
 * The type Cancelable entity.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class Cancelable<I> extends AbstractEntity<I> {

    @Builder.Default
    @CassandraType(type = CassandraType.Name.BOOLEAN)
    @Column(ComSchemaColumnConstantName.C_CHECK_CANCEL)
    private Boolean checkCancel = Boolean.FALSE;

    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @Column(ComSchemaColumnConstantName.C_CANCEL_DATE)
    private Date cancelDate;

    @CassandraType(type = CassandraType.Name.BIGINT)
    @Column(ComSchemaColumnConstantName.C_CANCELED_BY)
    private Long canceledBy;

    /**
     * Sets check cancel.
     *
     * @param checkCancel the check cancel
     */
    public void setCheckCancel(Boolean checkCancel) {
        this.checkCancel = (checkCancel != null && checkCancel);
    }
}
