package eu.isygoit.dto.extendable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Licence model dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class LicenceModelDto<T extends Serializable> extends AuditableDto<T> {

    private String tenant;
    private String provider;
    private String type;
    private String user;
    private Date expiryDate;
    private Long crc16;
    private Long crc32;
}
