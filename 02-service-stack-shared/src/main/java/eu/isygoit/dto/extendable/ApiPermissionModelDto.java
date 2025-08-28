package eu.isygoit.dto.extendable;

import eu.isygoit.enums.IEnumEnabledBinaryStatus;
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
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ApiPermissionModelDto<T extends Serializable> extends AuditableDto<T> {

    private String serviceName;
    private String object;
    private String method;
    private IEnumRequest.Types rqType;
    private String path;
    private String description;
    @Builder.Default
    private IEnumEnabledBinaryStatus.Types status = IEnumEnabledBinaryStatus.Types.ENABLED;
}
