package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private String mimetype;
    private Long size;
    private Long version;
    private Map<String, String> tags;
    /**
     * Audit of the stored file itself (not of the record that owns it); null for linked files, which have their own dates.
     */
    private LocalDateTime fileCreateDate;
    private LocalDateTime fileUpdateDate;

    public Map<String, String> getTags() {
        if (!CollectionUtils.isEmpty(tags)) {
            return tags;
        }
        tags = new LinkedHashMap<>();
        tags.put("mimetype", mimetype != null ? mimetype : "NA");
        tags.put("category", category != null ? category : "NA");
        tags.put("extension", extension != null ? extension : "NA");
        return tags;
    }
}
