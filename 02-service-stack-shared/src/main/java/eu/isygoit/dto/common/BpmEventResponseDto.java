package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Bpm event response dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BpmEventResponseDto extends AbstractDto {

    private Boolean accepted;
    private String status;

}
