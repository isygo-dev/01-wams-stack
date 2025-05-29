package eu.isygoit.model;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.converter.CamelCaseConverter;
import eu.isygoit.converter.LowerCaseConverter;
import eu.isygoit.listener.TimeLineListener;
import eu.isygoit.model.jakarta.AuditableCancelableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.validation.constraints.Email;
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
        implements IDomainAssignable, ICodeAssignable, ITLEntity, IFileEntity, IMultiFileEntity<ResumeLinkedFile>, IImageEntity {

    private Long id;
    private String domain;
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
