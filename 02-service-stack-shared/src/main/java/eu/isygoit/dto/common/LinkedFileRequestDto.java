package eu.isygoit.dto.common;

import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * The type Linked file request dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileRequestDto extends AbstractAuditableDto<Long> implements IFileUploadDto {

    private String tenant;
    private String code;                //Unique file name
    private String originalFileName;    //original file name
    private String path;
    private List<String> tags;
    private List<String> categoryNames;
    private MultipartFile file;
}
