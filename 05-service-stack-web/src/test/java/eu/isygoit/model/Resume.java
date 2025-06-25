package eu.isygoit.model;

import eu.isygoit.model.jakarta.AuditableCancelableEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * The type Resume.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
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
    private String type;
    private List<String> tags;
    private List<ResumeLinkedFile> additionalFiles;
}
