package eu.isygoit.dto;

import eu.isygoit.dto.extendable.IdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginEventDto extends IdAssignableDto<UUID> {

    @Setter
    private UUID id;
    private String userId;
    private String ip;
    private String device;
}
