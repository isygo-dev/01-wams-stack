package eu.isygoit.dto.extendable;

import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.enums.IEnumBinaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Customer model dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class CustomerModelDto<I extends Serializable> extends AbstractAuditableDto<I> implements IImageUploadDto {

    private String name;
    private String description;
    private String url;
    private String email;
    private String phoneNumber;
    private String imagePath;
    private String domain;
    @Builder.Default
    private IEnumBinaryStatus.Types adminStatus = IEnumBinaryStatus.Types.ENABLED;
}
