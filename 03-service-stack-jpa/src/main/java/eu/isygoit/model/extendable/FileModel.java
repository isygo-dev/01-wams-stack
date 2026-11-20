package eu.isygoit.model.extendable;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
public abstract class FileModel<T extends Serializable> extends AuditableEntity<T> implements IFileEntity {

    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_FILE_NAME, updatable = false, length = ComSchemaConstantSize.FILE_NAME_SIZE)
    private String fileName;

    @Criteria(2)
    @Column(name = ComSchemaColumnConstantName.C_ORIGINAL_FILE_NAME, length = ComSchemaConstantSize.FILE_NAME_SIZE)
    private String originalFileName;

    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_PATH, updatable = false)
    private String path;

    @Criteria(3)
    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_EXTENSION, length = ComSchemaConstantSize.EXTENSION_SIZE)
    private String extension;

    @Column(name = ComSchemaColumnConstantName.C_TYPE, length = ComSchemaConstantSize.S_NAME)
    private String mimetype;

    @Column(name = ComSchemaColumnConstantName.C_CRC_16, updatable = false, nullable = true)
    private Long crc16;

    @Column(name = ComSchemaColumnConstantName.C_CRC_32, updatable = false, nullable = true)
    private Long crc32;

    @Column(name = ComSchemaColumnConstantName.C_SIZE, updatable = false, nullable = false)
    private Long size;

    @OrderBy(ComSchemaColumnConstantName.C_VERSION + " ASC")
    @Column(name = ComSchemaColumnConstantName.C_VERSION, updatable = false, nullable = true)
    private Long version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = ComSchemaColumnConstantName.C_TAG, columnDefinition = "jsonb")
    private Map<String, String> tags;

    @Criteria(1)
    @Column(name = ComSchemaColumnConstantName.C_CATEGORY, length = ComSchemaConstantSize.S_NAME)
    private String category;

    public Map<String, String> getTags() {
        if (!CollectionUtils.isEmpty(tags)) {
            return tags;
        }
        tags = new LinkedHashMap<>();
        tags.put("mimetype", mimetype != null ? mimetype : "NA");
        tags.put("category", category != null ? category : "NA");
        tags.put("extension", extension != null ? extension : "NA");
        return tags;
    }

    @Override
    public LocalDateTime getFileCreateDate() {
        return this.getCreateDate();
    }

    @Override
    public LocalDateTime getFileUpdateDate() {
        return this.getUpdateDate();
    }
}