package eu.isygoit.model.extendable;


import eu.isygoit.model.LinkedFile;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file model.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class LinkedModelFile<I extends Serializable> extends AssignableFile<I> implements LinkedFile {

    @Column(name = ComSchemaColumnConstantName.C_CRC_16, updatable = false, nullable = true)
    private Long crc16;

    @Column(name = ComSchemaColumnConstantName.C_CRC_32, updatable = false, nullable = true)
    private Long crc32;

    @Column(name = ComSchemaColumnConstantName.C_SIZE, updatable = false, nullable = false)
    private Long size;

    @OrderBy(ComSchemaColumnConstantName.C_VERSION + " ASC")
    @Column(name = ComSchemaColumnConstantName.C_VERSION, updatable = false, nullable = true)
    private Long version;

    @Column(name = ComSchemaColumnConstantName.C_MIMETYPE, updatable = false, nullable = true)
    private String mimetype;
}
