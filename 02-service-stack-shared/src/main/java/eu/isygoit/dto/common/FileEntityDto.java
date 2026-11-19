package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The type File entity dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class FileEntityDto<T extends Serializable> extends AuditableIdAssignableDto<T> {

    private String category;
    private String fileName;
    private String originalFileName;
    private String path;
    private String extension;
    private String type;
    private Long size;
    private Long version;
    private String mimetype;
    private List<String> tags;

    public List<String> getTags() {
        if(CollectionUtils.isEmpty(tags)) {
            return Arrays.asList(type, category, extension);
        }
        return tags;
    }
}
