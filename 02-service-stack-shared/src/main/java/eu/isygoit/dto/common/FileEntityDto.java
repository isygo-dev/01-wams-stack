package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type File entity dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class FileEntityDto<T extends Serializable> extends AuditableDto<T> {

    private String fileName;
    private String originalFileName;
    private String path;
    private String extension;
    private String type;
}
