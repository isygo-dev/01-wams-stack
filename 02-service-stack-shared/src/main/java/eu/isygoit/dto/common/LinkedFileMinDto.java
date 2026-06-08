package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file min dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileMinDto<T extends Serializable> extends AuditableIdAssignableDto<T> {

    @Setter
    private T id;
    private String code;
    private String originalFileName;
    private Long size;
    private Long version;
}
