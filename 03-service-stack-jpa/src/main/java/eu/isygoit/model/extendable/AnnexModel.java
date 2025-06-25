package eu.isygoit.model.extendable;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.enums.IEnumLanguage;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * The type Annex model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AnnexModel<T extends Serializable> extends AuditableEntity<T> implements ITenantAssignable {

    @Length(max = ComSchemaConstantSize.TABLE_CODE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_CODE, length = ComSchemaConstantSize.TABLE_CODE, nullable = false)
    private String tableCode;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'EN'")
    @Column(name = ComSchemaColumnConstantName.C_LANGUAGE_CODE, length = IEnumLanguage.STR_ENUM_SIZE, nullable = false)
    private IEnumLanguage.Types language;

    @Length(max = ComSchemaConstantSize.XL_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_VALUE, length = ComSchemaConstantSize.XL_VALUE, nullable = false)
    private String value;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Length(max = ComSchemaConstantSize.S_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_REFERENCE, length = ComSchemaConstantSize.S_VALUE)
    private String reference;
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_ORDER)
    private Integer annexOrder;
    @ColumnDefault("'" + TenantConstants.DEFAULT_TENANT_NAME + "'")
    @Column(name = ComSchemaColumnConstantName.C_TENANT, length = ComSchemaConstantSize.TENANT, updatable = false, nullable = false)
    private String tenant;
}
