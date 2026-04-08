package eu.isygoit.dto.extendable;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Abstract auditable dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AuditableDto<T extends Serializable> extends IdAssignableDto<T> {

    private Date createDate;
    private String createdBy;
    private Date updateDate;
    private String updatedBy;
}