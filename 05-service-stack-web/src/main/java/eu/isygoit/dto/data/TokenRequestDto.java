package eu.isygoit.dto.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * The type Token request dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TokenRequestDto {

    private String subject;
    private Map<String, Object> claims;
}
