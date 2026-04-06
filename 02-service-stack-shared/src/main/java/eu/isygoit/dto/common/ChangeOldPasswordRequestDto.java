package eu.isygoit.dto.common;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Change old password request dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ChangeOldPasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
