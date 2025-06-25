package eu.isygoit.model.extendable;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.enums.IEnumEnabledBinaryStatus;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.jakarta.AuditableEntity;
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
 * The type Customer model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class CustomerModel<T extends Serializable> extends AuditableEntity<T> implements ITenantAssignable, ICodeAssignable, IImageEntity {

    //@Convert(converter = LowerCaseConverter.class)
    @ColumnDefault("'" + TenantConstants.DEFAULT_TENANT_NAME + "'")
    @Column(name = ComSchemaColumnConstantName.C_TENANT, length = ComSchemaConstantSize.TENANT, updatable = false, nullable = false)
    private String tenant;

    //@Convert(converter = LowerCaseConverter.class)
    @Column(name = ComSchemaColumnConstantName.C_CODE, length = ComSchemaConstantSize.CODE, updatable = false, nullable = false)
    private String code;

    @Column(name = ComSchemaColumnConstantName.C_NAME, length = ComSchemaConstantSize.S_NAME)
    private String name;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Column(name = ComSchemaColumnConstantName.C_URL)
    private String url;

    @Column(name = ComSchemaColumnConstantName.C_EMAIL)
    private String email;

    @Column(name = ComSchemaColumnConstantName.C_PHONE_NUMBER)
    private String phoneNumber;

    @Column(name = ComSchemaColumnConstantName.C_LOGO)
    private String imagePath;

    @Builder.Default
    @ColumnDefault("'ENABLED'")
    @Enumerated(EnumType.STRING)
    @Column(name = ComSchemaColumnConstantName.C_ADMIN_STATUS, length = IEnumEnabledBinaryStatus.STR_ENUM_SIZE, nullable = false)
    private IEnumEnabledBinaryStatus.Types adminStatus = IEnumEnabledBinaryStatus.Types.ENABLED;
}
