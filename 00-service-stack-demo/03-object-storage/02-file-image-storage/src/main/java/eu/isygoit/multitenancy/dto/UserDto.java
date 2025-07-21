package eu.isygoit.multitenancy.dto;

import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.extendable.AbstractAuditableDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends AbstractAuditableDto<Long> implements IImageUploadDto {

    @NotNull
    private String tenant;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    @Builder.Default
    private boolean active = Boolean.FALSE;

    private String imagePath;
}
