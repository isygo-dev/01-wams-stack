package eu.isygoit.model.extendable;

import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Locale message model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class LocaleMessageModel<T extends Serializable> implements IIdEntity<T> {

    @Id
    @SequenceGenerator(name = "message_sequence_generator", sequenceName = "message_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_sequence_generator")
    private T id;

    @Column(name = ComSchemaColumnConstantName.C_CODE, length = ComSchemaConstantSize.XXL_VALUE, nullable = false)
    private String code;
    @Column(name = ComSchemaColumnConstantName.C_LOCALE, length = ComSchemaConstantSize.LANG_CODE, nullable = false)
    private String locale;
    @Column(name = ComSchemaColumnConstantName.C_TEXT, length = ComSchemaConstantSize.XXL_VALUE, nullable = false)
    private String text;
}
