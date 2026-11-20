package eu.isygoit.model;

import eu.isygoit.model.jakarta.AuditableCancelableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.Column;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The type Resume.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Resume extends AuditableCancelableEntity<Long>
        implements ITenantAssignable, ICodeAssignable, ITLEntity, IFileEntity, IMultiFileEntity<ResumeLinkedFile>, IImageEntity {

    private Long id;
    private String tenant;
    private String code;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String imagePath;
    private String source;
    private String title;
    private String presentation;

    //BEGIN IFileEntity : SecondaryTable / ResumeFile
    private String fileName;
    private String originalFileName;
    private String path;
    private String extension;
    private String mimetype;
    private Long crc16;
    private Long crc32;
    private Long size;
    private Long version;
    private Map<String, String> tags;
    private String category;
    private List<ResumeLinkedFile> additionalFiles;
    private LocalDateTime fileCreateDate;
    private LocalDateTime fileUpdateDate;
    //END IFileEntity : SecondaryTable / ResumeFile
}
