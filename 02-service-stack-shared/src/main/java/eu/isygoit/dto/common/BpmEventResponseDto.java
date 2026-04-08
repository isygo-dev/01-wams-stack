package eu.isygoit.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The type Bpm event response dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BpmEventResponseDto {

    @Setter
    private Long id;
    private Boolean accepted;
    private String status;
}
