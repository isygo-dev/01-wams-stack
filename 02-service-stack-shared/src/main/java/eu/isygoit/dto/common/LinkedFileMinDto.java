package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file min dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileMinDto<T extends Serializable> extends AuditableDto<T> {

    private T id;
    private String code;
    private String originalFileName;
    private Long size;
    private Long version;
}
