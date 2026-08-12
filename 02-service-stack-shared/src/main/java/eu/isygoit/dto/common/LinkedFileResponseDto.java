package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The type Linked file response dto.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileResponseDto extends AuditableDto {

    private String tenant;
    private String code;                //Unique file name
    private String originalFileName;    //original file name
    private String path;
    private List<String> tags;
    private List<String> categoryNames;
}
