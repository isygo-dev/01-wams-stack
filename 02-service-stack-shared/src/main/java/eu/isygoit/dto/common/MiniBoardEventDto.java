package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The type Mini board event dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MiniBoardEventDto extends AbstractDto<Long> {

    @Setter
    private Long id;
    private String title;
    private String type;
}
