package eu.isygoit.model.extendable;


import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.ILinkedFile;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class LinkedFileModel<T extends Serializable> extends FileModel<T> implements ILinkedFile, ICodeAssignable {

    //@Convert(converter = LowerCaseConverter.class)
    @Criteria(4)
    @Column(name = ComSchemaColumnConstantName.C_CODE, length = ComSchemaConstantSize.CODE, updatable = false, nullable = false)
    private String code;
}
