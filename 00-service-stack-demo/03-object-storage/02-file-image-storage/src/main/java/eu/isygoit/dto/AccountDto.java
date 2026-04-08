package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto extends AuditableDto<Long> {

    @Setter
    private Long id;
    @NotNull
    private String tenant;
    @NotNull
    private String login;
    @NotNull
    @Email
    private String email;
    @NotNull
    private String passkey;
}
