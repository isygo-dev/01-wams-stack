package eu.isygoit.storage.object;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * The type File storage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStorage {

    /**
     * The Object name.
     */
    public String objectName;
    /**
     * The Size.
     */
    public long size;
    /**
     * The Etag.
     */
    public String etag;
    /**
     * The Last modified.
     */
    public ZonedDateTime lastModified;
    /**
     * The Tags.
     */
    public List<String> tags;

    /**
     * The Version id.
     */
    public String versionID;

    /**
     * The Current version.
     */
    public boolean currentVersion;

    /**
     * The Metadata.
     */
    public Map<String, String> metadata;

    /**
     * The Path type.
     */
    public String pathType;
}


