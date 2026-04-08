package eu.isygoit.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The type Bpm event request dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BpmEventRequestDto {

    @Setter
    private Long id;
    private BoardItemModelDto item;
    private String wbCode;
    private String fromState; //code
    private String toState; // code
}
