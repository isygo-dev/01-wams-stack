package eu.isygoit.model.jakarta;

import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The type Cancelable entity.
 *
 * @param <T> the type parameter
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SQLRestriction("check_cancel = false")
public abstract class CancelableEntity<T extends Serializable> extends AbstractEntity<T> {

    @Builder.Default
    @ColumnDefault("'false'")
    @Column(name = ComSchemaColumnConstantName.C_CHECK_CANCEL, nullable = false)
    private Boolean checkCancel = Boolean.FALSE;

    @Column(name = ComSchemaColumnConstantName.C_CANCEL_DATE)
    private LocalDateTime cancelDate;

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
