package eu.isygoit.dto.extendable;

import eu.isygoit.enums.IEnumBinaryStatus;
import eu.isygoit.enums.IEnumRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Api permission model dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ApiPermissionModelDto<I extends Serializable> extends AbstractAuditableDto<I> {

    private String serviceName;
    private String object;
    private String method;
    private IEnumRequest.Types rqType;
    private String path;
    private String description;
    @Builder.Default
    private IEnumBinaryStatus.Types status = IEnumBinaryStatus.Types.ENABLED;
}
