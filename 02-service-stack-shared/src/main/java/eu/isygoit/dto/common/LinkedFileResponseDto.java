package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Linked file response dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileResponseDto extends AbstractDto {

    private String code;
}
