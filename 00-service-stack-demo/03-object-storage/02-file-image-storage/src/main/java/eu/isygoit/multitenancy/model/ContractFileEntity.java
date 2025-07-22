package eu.isygoit.multitenancy.model;

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
@Table(name = "CONTRACT_FILE")
public class ContractFileEntity extends FileEntity<Long> implements IFileEntity {

    @Id
    @SequenceGenerator(name = "contract_file_sequence_generator", sequenceName = "contract_file_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_file_sequence_generator")
    @Column(name = ComSchemaColumnConstantName.C_ID, updatable = false, nullable = false)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "CONTRACT_FILE_TAGS"
            , joinColumns = @JoinColumn(name = "CONTRACT",
            referencedColumnName = ComSchemaColumnConstantName.C_ID,
            foreignKey = @ForeignKey(name = "FK_TAGS_REF_CONTRACT_FILE")))
    @Column(name = ComSchemaColumnConstantName.C_TAG_OWNER)
    private List<String> tags;
}