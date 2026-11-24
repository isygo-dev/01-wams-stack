package eu.isygoit.model.imagefile;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.model.*;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.multifile.ResumeLinkedFile;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "RESUME")
@SecondaryTable(name = "RESUME_FILE",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = ComSchemaColumnConstantName.C_ID,
                referencedColumnName = ComSchemaColumnConstantName.C_ID)
)
public class ResumeEntity extends AuditableEntity<Long> implements ITenantAssignable,
        IImageEntity,
        ISingleFileEntity,
        IMultiFileEntity<ResumeLinkedFile>,
        ICodeAssignable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resume_sequence_generator")
    @SequenceGenerator(name = "resume_sequence_generator", sequenceName = "resume_sequence", allocationSize = 1)

    private Long id;


    @Column(name = "TENANT_ID", nullable = false, updatable = false)
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
    private String mimetype;

    @Column(name = ComSchemaColumnConstantName.C_CRC_16, table = "RESUME_FILE")
    private Long crc16;

    @Column(name = ComSchemaColumnConstantName.C_CRC_32, table = "RESUME_FILE")
    private Long crc32;

    @Column(name = ComSchemaColumnConstantName.C_SIZE, table = "RESUME_FILE")
    private Long size;

    @OrderBy(ComSchemaColumnConstantName.C_VERSION + " ASC")
    @Column(name = ComSchemaColumnConstantName.C_VERSION, table = "RESUME_FILE")
    private Long version;
    /**
     * Stored as a single JSON column (e.g. {"mimetype":"…","category":"…","extension":"…"}).
     * Keeps the existing C_TAG column — no join table required.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = ComSchemaColumnConstantName.C_TAG, table = "RESUME_FILE")
    private Map<String, String> tags;

    @Criteria(1)
    @Column(name = ComSchemaColumnConstantName.C_CATEGORY, table = "RESUME_FILE")
    private String fileCategory;

    /**
     * When the stored file was first uploaded (file audit, in the file table).
     */
    @CreatedDate
    @Column(name = ComSchemaColumnConstantName.C_CREATE_DATE, table = "RESUME_FILE")
    private LocalDateTime fileCreateDate;

    /**
     * When the stored file was last uploaded or replaced (file audit, in the file table).
     */
    @LastModifiedDate
    @Column(name = ComSchemaColumnConstantName.C_UPDATE_DATE, table = "RESUME_FILE")
    private LocalDateTime fileUpdateDate;
    //END IFileEntity : SecondaryTable

    //Multi file join
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "RESUME", referencedColumnName = ComSchemaColumnConstantName.C_ID
            , foreignKey = @ForeignKey(name = "FK_ADDITIONAL_FILE_REF_RESUME"))
    private List<ResumeLinkedFile> additionalFiles;
}