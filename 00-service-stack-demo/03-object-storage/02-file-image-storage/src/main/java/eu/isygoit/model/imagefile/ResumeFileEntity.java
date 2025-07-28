package eu.isygoit.model.imagefile;

import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.extendable.FileEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RESUME_FILE")
public class ResumeFileEntity extends FileEntity<Long> implements IFileEntity {

    @Id
    @SequenceGenerator(name = "resume_file_sequence_generator", sequenceName = "resume_file_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resume_file_sequence_generator")
    @Column(name = ComSchemaColumnConstantName.C_ID, updatable = false, nullable = false)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "RESUME_FILE_TAGS"
            , joinColumns = @JoinColumn(name = "RESUME",
            referencedColumnName = ComSchemaColumnConstantName.C_ID,
            foreignKey = @ForeignKey(name = "FK_TAGS_REF_RESUME_FILE")))
    @Column(name = ComSchemaColumnConstantName.C_TAG_OWNER)
    private List<String> tags;
}