package eu.isygoit.model;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaTableConstantName;
import eu.isygoit.model.schema.ComSchemaUcConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * The type App next code.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = ComSchemaTableConstantName.T_APP_NEXT_CODE
        , uniqueConstraints = {@UniqueConstraint(name = ComSchemaUcConstantName.UC_NEXT_CODE_ENTITY
        , columnNames = {ComSchemaColumnConstantName.C_ENTITY, ComSchemaColumnConstantName.C_ATTRIBUTE, ComSchemaColumnConstantName.C_TENANT})})
@ToString(callSuper = true)
public class AppNextCode extends NextCodeModel<Long> {

    @Id
    @SequenceGenerator(name = "next_code_sequence_generator", sequenceName = "next_code_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "next_code_sequence_generator")
    @Column(name = ComSchemaColumnConstantName.C_ID, updatable = false, nullable = false)
    private Long id;
}
