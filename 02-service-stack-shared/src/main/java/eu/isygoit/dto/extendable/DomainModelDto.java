package eu.isygoit.dto.extendable;

import eu.isygoit.enums.IEnumBinaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Domain model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class DomainModelDto<T extends Serializable> extends AbstractAuditableDto<T> {

    private String name;
    private String description;
    private String url;
    @Builder.Default
    private IEnumBinaryStatus.Types adminStatus = IEnumBinaryStatus.Types.ENABLED;
}
