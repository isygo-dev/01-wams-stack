package eu.isygoit.dto.common;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Reset pwd via token request dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResetPwdViaTokenRequestDto {

    private String token;
    private String password;
    private String fullName;
    private String application;
}
