package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * The type Board item model dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BoardItemModelDto extends AuditableDto<Long> {

    private Long id;
    private String code;
    private String state;
    private String itemName;
    private String imagePath;
    private String itemImage;
    private List<MiniBoardEventDto> events;
}
