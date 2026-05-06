package eu.isygoit.s3.object;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Représente les métadonnées d'un fichier MinIO.
 * <p>
 * Certaines propriétés (objectName, bucketName, path, contentType, tagsMap) sont utilisées pour l'upload.
 * Les autres (size, etag, versionID, lastModified, tags, etc.) sont renseignées après l'upload ou lors de la lecture.
 */
@Data
@Builder
public class MetaData {
    private String objectName;
    private String bucketName;
    private String path;
    private long size;
    private String contentType;
    private String etag;
    private String versionID;
    private boolean currentVersion;
    private List<String> tags;
    private Map<String, String> tagsMap;
    private String lastModified;
}
