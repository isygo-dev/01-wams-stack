package eu.isygoit.dto.extendable;

import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.enums.IEnumEnabledBinaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Customer model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class CustomerModelDto<T extends Serializable> extends AuditableDto<T> implements IImageUploadDto {

    private String name;
    private String description;
    private String url;
    private String email;
    private String phoneNumber;
    private String imagePath;
    private String tenant;
    @Builder.Default
    private IEnumEnabledBinaryStatus.Types adminStatus = IEnumEnabledBinaryStatus.Types.ENABLED;
}
