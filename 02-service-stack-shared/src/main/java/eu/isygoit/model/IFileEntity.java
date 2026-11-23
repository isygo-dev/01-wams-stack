package eu.isygoit.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The interface File entity.
 */
public interface IFileEntity {

    String getMimetype();

    void setMimetype(String type);

    String getFileName();

    void setFileName(String fileName);

    String getOriginalFileName();

    void setOriginalFileName(String originalFileName);

    String getPath();

    void setPath(String path);

    String getExtension();

    void setExtension(String extension);

    Map<String, String> getTags();

    String getCategory();

    void setCategory(String category);

    LocalDateTime getFileUpdateDate();

    LocalDateTime getFileCreateDate();

    Long getCrc16();

    void setCrc16(Long crc16);

    Long getCrc32();

    void setCrc32(Long crc32);

    Long getSize();

    void setSize(Long size);

    Long getVersion();

    void setVersion(Long version);
}
