package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserDto extends AuditableIdAssignableDto<Long> implements IImageUploadDto {


    private Long id;
    @NotNull
    private String tenant;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    @Builder.Default
    private boolean active = Boolean.FALSE;

    //IImageUploadDto fields
    private String imagePath;
}
