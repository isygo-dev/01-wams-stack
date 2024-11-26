package eu.isygoit.model.jakarta;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Cancelable entity.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class CancelableEntity<T extends Serializable> extends AbstractEntity<T> {

    @Builder.Default
    @ColumnDefault("'false'")
    @Column(name = ComSchemaColumnConstantName.C_CHECK_CANCEL, nullable = false)
    private Boolean checkCancel = Boolean.FALSE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = ComSchemaColumnConstantName.C_CANCEL_DATE)
    private Date cancelDate;

    @Column(name = ComSchemaColumnConstantName.C_CANCELED_BY)
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
