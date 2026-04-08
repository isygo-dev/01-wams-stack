package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends AuditableDto<Long> implements IImageUploadDto {

    @Setter
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
