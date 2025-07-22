package eu.isygoit.multitenancy.model.imagefile;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RESUME")
@SecondaryTable(name = "RESUME_FILE",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = ComSchemaColumnConstantName.C_ID,
                referencedColumnName = ComSchemaColumnConstantName.C_ID)
)
public class ResumeEntity extends AuditableEntity<Long> implements ITenantAssignable, IImageEntity, IFileEntity, ICodeAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resume_seq_generator")
    @SequenceGenerator(name = "resume_seq_generator", sequenceName = "resume_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false)
    private String tenant;

    //ICodeAssignable fields (should implement setCode & getCode)
    @Column(name = "CODE", nullable = false)
    private String code;

    @Criteria
    @Column(name = "TITLE", nullable = false)
    private String title;


    @Criteria
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Criteria
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Criteria
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = Boolean.FALSE;

    //IImageEntity fields (should implement setImagePath & getImagePath)
    @Column(name = "IMAGE_PATH")
    private String imagePath;

    //BEGIN IFileEntity : SecondaryTable / MsgTemplateFile
    @Column(name = ComSchemaColumnConstantName.C_FILE_NAME, table = "RESUME_FILE")
    private String fileName;
    @Column(name = ComSchemaColumnConstantName.C_ORIGINAL_FILE_NAME, table = "RESUME_FILE")
    private String originalFileName;
    @ColumnDefault("'NA'")
    @Column(name = ComSchemaColumnConstantName.C_PATH, table = "RESUME_FILE")
    private String path;
    @Column(name = ComSchemaColumnConstantName.C_EXTENSION, table = "RESUME_FILE")
    private String extension;
    @Column(name = ComSchemaColumnConstantName.C_TYPE, table = "RESUME_FILE")
    private String type;

    @ElementCollection
    @CollectionTable(name = "RESUME_FILE_TAGS"
            , joinColumns = @JoinColumn(name = "RESUME",
            referencedColumnName = ComSchemaColumnConstantName.C_ID,
            foreignKey = @ForeignKey(name = "FK_TAGS_REF_RESUME_FILE")))
    @Column(name = ComSchemaColumnConstantName.C_TAG_OWNER)
    private List<String> tags;
    //END IFileEntity : SecondaryTable
}