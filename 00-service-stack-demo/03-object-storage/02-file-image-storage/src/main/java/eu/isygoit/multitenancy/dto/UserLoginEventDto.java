package eu.isygoit.multitenancy.dto;

import eu.isygoit.dto.extendable.IdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginEventDto extends IdAssignableDto<UUID> {

    private String userId;
    private String ip;
    private String device;
}
