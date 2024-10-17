package eu.isygoit.model.extendable;

import eu.isygoit.constants.DomainConstants;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Licence model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class LicenceModel<T extends Serializable> extends AuditableEntity<T> {

    @Column(name = ComSchemaColumnConstantName.C_PROVIDER)
    private String provider;

    @Column(name = ComSchemaColumnConstantName.C_TYPE, nullable = false)
    private String type;

    //@Convert(converter = LowerCaseConverter.class)
    @ColumnDefault("'" + DomainConstants.DEFAULT_DOMAIN_NAME + "'")
    @Column(name = ComSchemaColumnConstantName.C_DOMAIN, length = ComSchemaConstantSize.DOMAIN, updatable = false, nullable = false)
    private String domain;

    @Column(name = ComSchemaColumnConstantName.C_USER)
    private String user;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = ComSchemaColumnConstantName.C_EXPIRY_DATE, updatable = false)
    private Date expiryDate;

    @Column(name = ComSchemaColumnConstantName.C_CRC16, updatable = false)
    private Integer crc16;

    @Column(name = ComSchemaColumnConstantName.C_CRC32, updatable = false)
    private Integer crc32;
}
