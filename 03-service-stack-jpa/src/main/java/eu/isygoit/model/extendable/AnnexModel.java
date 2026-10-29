package eu.isygoit.model.extendable;

import eu.isygoit.annotation.Criteria;
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
import java.util.Locale;

/**
 * The type Annex model.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class AnnexModel<T extends Serializable> extends AuditableEntity<T> implements ITenantAssignable {

    @ColumnDefault("'" + TenantConstants.DEFAULT_TENANT_NAME + "'")
    @Column(name = ComSchemaColumnConstantName.C_TENANT, length = ComSchemaConstantSize.TENANT, updatable = false, nullable = false)
    private String tenant;

    @Criteria(1)
    @Length(max = ComSchemaConstantSize.TABLE_CODE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_CODE, length = ComSchemaConstantSize.TABLE_CODE, nullable = false)
    private String tableCode;

    @Length(max = ComSchemaConstantSize.XL_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_VALUE, length = ComSchemaConstantSize.XL_VALUE, nullable = false)
    private String value;

    @Criteria(2)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'EN'")
    @Column(name = ComSchemaColumnConstantName.C_LANGUAGE_CODE, length = IEnumLanguage.STR_ENUM_SIZE, nullable = false)
    private IEnumLanguage.Types language;

    @Length(max = ComSchemaConstantSize.XL_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_TRANSLATED, length = ComSchemaConstantSize.XL_VALUE, nullable = false)
    private String translated;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Criteria(3)
    @Length(max = ComSchemaConstantSize.S_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_ANNEX_REFERENCE, length = ComSchemaConstantSize.S_VALUE)
    private String reference;

    @Column(name = ComSchemaColumnConstantName.C_ANNEX_ORDER)
    private Integer annexOrder;

    /**
     * Returns the stored {@code translated} value when present; otherwise builds
     * a deterministic fallback from the row itself.
     *
     * <p>Fallback shape: {@code <language_code>_<value>}, all lowercase, where
     * {@code <language_code>} is {@link IEnumLanguage.Types#meaning()} (already
     * {@code "fr"}, {@code "en"}, {@code "ar"}, …) and {@code <value>} is
     * lowercased with {@link Locale#ROOT} so accented and non-Latin scripts are
     * preserved.
     *
     * <p>Edge cases:
     * <ul>
     *   <li>{@code translated} blank (null, empty, whitespace) → fallback applies</li>
     *   <li>{@code language == null} → {@code "en"} is used (matches the column default)</li>
     *   <li>{@code value == null} → treated as empty, so the result is {@code "<code>_"}</li>
     * </ul>
     *
     * @return the translated text, or the lowercase {@code <language_code>_<value>} fallback
     */
    public String getTranslated() {
        if (translated != null && !translated.isBlank()) {
            return translated;
        }
        String lang = (language != null && language.meaning() != null && !language.meaning().isBlank())
                ? language.meaning()
                : "en";
        String raw = value == null ? "" : value;
        return (lang + "_" + raw).toLowerCase(Locale.ROOT);
    }
}