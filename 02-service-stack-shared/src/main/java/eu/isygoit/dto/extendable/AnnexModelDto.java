package eu.isygoit.dto.extendable;

import eu.isygoit.enums.IEnumLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Annex model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AnnexModelDto<T extends Serializable> extends AuditableDto<T> {

    private String tableCode;
    private IEnumLanguage.Types language;
    private String value;
    private String description;
    private String reference;
    private Integer annexOrder;
}
