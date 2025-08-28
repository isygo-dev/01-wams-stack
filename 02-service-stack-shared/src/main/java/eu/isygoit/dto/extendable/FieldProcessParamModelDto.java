package eu.isygoit.dto.extendable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Field process param model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class FieldProcessParamModelDto<T extends Serializable> extends AuditableDto<T> {

    private String processName;
    private String fieldName;
    private String description;
    private Boolean required;
    private Integer length;
    private String defaultValue;
    private Boolean readOnly;
    private String pattern;
}
