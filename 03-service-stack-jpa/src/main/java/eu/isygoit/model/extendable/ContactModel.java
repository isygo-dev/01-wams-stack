package eu.isygoit.model.extendable;

import eu.isygoit.enums.IEnumContact;
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
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * The type Contact model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class ContactModel<T extends Serializable> extends AuditableEntity<T> {

    @Enumerated(EnumType.STRING)
    @Column(name = ComSchemaColumnConstantName.C_CONTACT_TYPE, length = IEnumContact.STR_ENUM_SIZE, nullable = false)
    private IEnumContact.Types type;

    @Length(max = ComSchemaConstantSize.L_VALUE)
    @Column(name = ComSchemaColumnConstantName.C_CONTACT_VALUE, length = ComSchemaConstantSize.L_VALUE, nullable = false)
    private String value;
}
