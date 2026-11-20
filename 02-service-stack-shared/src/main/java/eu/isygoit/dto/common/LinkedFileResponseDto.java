package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * The type Linked file response dto.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileResponseDto extends AuditableDto {

    private String tenant;
    private String code;                //Unique file name
    private String originalFileName;    //original file name
    private String path;
    private Map<String, String> tags;
    private String category;
}
