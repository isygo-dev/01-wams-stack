package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AbstractDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.core.io.Resource;

/**
 * The type Token dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResourceDto extends AbstractDto {

    @NotNull
    private String originalFileName;
    @NotEmpty
    private String fileName;
    @NotEmpty
    private String fileType;

    private Resource resource;
}
