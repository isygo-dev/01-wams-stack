package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Reset pwd via token request dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResetPwdViaTokenRequestDto extends AbstractDto {

    private String token;
    private String password;
    private String fullName;
    private String application;
}
