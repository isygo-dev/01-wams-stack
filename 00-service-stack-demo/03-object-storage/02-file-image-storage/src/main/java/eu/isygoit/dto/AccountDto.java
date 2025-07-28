package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AbstractAuditableDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto extends AbstractAuditableDto<Long> {

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
