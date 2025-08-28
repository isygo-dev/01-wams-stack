package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ContractDto extends AuditableDto<Long> implements IFileUploadDto {

    private Long id;
    private String tenant;

    //ICodeAssignable fields (should implement setCode & getCode)
    private String code;

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active = Boolean.FALSE;

    //IFileUploadDto fields
    private String originalFileName;
}
