package eu.isygoit.dto.data;


import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * The type Token request dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TokenRequestDto extends AbstractDto {

    private String subject;
    private Map<String, Object> claims;
}
