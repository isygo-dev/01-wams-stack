package eu.isygoit.model.extendable;

import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The type File entity.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class FileEntity<T extends Serializable> extends AuditableEntity<T> implements IFileEntity, ICodifiable {

    //@Convert(converter = LowerCaseConverter.class)
    @Column(name = ComSchemaColumnConstantName.C_CODE, length = ComSchemaConstantSize.CODE, updatable = false)
    private String code;
    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_FILE_NAME, length = ComSchemaConstantSize.FILE_NAME_SIZE)
    private String fileName;
    @Column(name = ComSchemaColumnConstantName.C_ORIGINAL_FILE_NAME, length = ComSchemaConstantSize.FILE_NAME_SIZE)
    private String originalFileName;
    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_PATH, nullable = false)
    private String path;
    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_EXTENSION, length = ComSchemaConstantSize.EXTENSION_SIZE)
    private String extension;
    @Column(name = ComSchemaColumnConstantName.C_TYPE, length = ComSchemaConstantSize.S_NAME)
    private String type;

    @Override
    public List<String> getTags() {
        return Arrays.asList(type, extension);
    }
}
