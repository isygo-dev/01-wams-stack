package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ContractDto extends AuditableDto<Long> implements IFileUploadDto {

    @Setter
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
