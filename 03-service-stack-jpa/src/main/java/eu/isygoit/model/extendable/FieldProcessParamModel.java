package eu.isygoit.model.extendable;

import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * The type Field process param model.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class FieldProcessParamModel<I extends Serializable> extends AuditableEntity<I> {

    @Length(max = ComSchemaConstantSize.S_NAME)
    @Column(name = ComSchemaColumnConstantName.C_PROCESS_NAME, length = ComSchemaConstantSize.S_NAME, nullable = false)
    private String processName;

    @Length(max = ComSchemaConstantSize.S_NAME)
    @Column(name = ComSchemaColumnConstantName.C_FIELD_NAME, length = ComSchemaConstantSize.S_NAME, nullable = false)
    private String fieldName;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Builder.Default
    @ColumnDefault("'false'")
    @Column(name = ComSchemaColumnConstantName.C_REQUIRED, nullable = false)
    private Boolean required = Boolean.FALSE;

    @Column(name = ComSchemaColumnConstantName.C_LENGTH)
    private Integer length;

    @Length(max = ComSchemaConstantSize.M_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_DEFAULT_VALUE, length = ComSchemaConstantSize.M_VALUE)
    private String defaultValue;

    @Builder.Default
    @ColumnDefault("'false'")
    @Column(name = ComSchemaColumnConstantName.C_READONLY, nullable = false)
    private Boolean readOnly = Boolean.FALSE;

    @Column(name = ComSchemaColumnConstantName.C_PATTERN)
    private String pattern;
}
