package eu.isygoit.model.tenancy;

import eu.isygoit.enums.IEnumEnabledBinaryStatus;
import eu.isygoit.model.jakarta.AuditableCancelableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;

/**
 * The type Tenant model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class TenantModel<T extends Serializable> extends AuditableCancelableEntity<T> {

    @Column(name = ComSchemaColumnConstantName.C_NAME, length = ComSchemaConstantSize.S_NAME, updatable = false)
    private String name;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Column(name = ComSchemaColumnConstantName.C_URL, length = ComSchemaConstantSize.XL_VALUE)
    private String url;

    @Column(name = ComSchemaColumnConstantName.C_EMAIL, length = ComSchemaConstantSize.XL_VALUE)
    private String email;

    @Column(name = ComSchemaColumnConstantName.C_PHONE_NUMBER)
    private String phone;

    @Column(name = ComSchemaColumnConstantName.C_INDUSTRY, length = ComSchemaConstantSize.S_NAME)
    private String industry;

    @Builder.Default
    @ColumnDefault("'ENABLED'")
    @Enumerated(EnumType.STRING)
    @Column(name = ComSchemaColumnConstantName.C_ADMIN_STATUS, length = IEnumEnabledBinaryStatus.STR_ENUM_SIZE, nullable = false)
    private IEnumEnabledBinaryStatus.Types adminStatus = IEnumEnabledBinaryStatus.Types.ENABLED;
}
