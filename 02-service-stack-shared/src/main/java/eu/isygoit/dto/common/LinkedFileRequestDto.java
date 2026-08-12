package eu.isygoit.dto.common;

import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The type Linked file request dto.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileRequestDto extends AuditableDto implements IFileUploadDto {

    private String code;                //Unique file name
    private String originalFileName;    //original file name
    private String path;
    private List<String> tags;
    private List<String> categoryNames;
    private MultipartFile file;
}
