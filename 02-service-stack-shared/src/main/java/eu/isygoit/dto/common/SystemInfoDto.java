package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type System info dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SystemInfoDto extends AbstractAuditableDto<Long> {

    private String name;
    private String version;
}
