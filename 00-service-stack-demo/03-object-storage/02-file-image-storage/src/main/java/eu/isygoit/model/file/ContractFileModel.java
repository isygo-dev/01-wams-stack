package eu.isygoit.model.file;

import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.extendable.FileModel;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "CONTRACT_FILE")
public class ContractFileModel extends FileModel<Long> implements IFileEntity {

    @Id
    @SequenceGenerator(name = "contract_file_sequence_generator", sequenceName = "contract_file_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_file_sequence_generator")
    @Column(name = ComSchemaColumnConstantName.C_ID, updatable = false, nullable = false)

    private Long id;
}