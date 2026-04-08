package eu.isygoit.model.multifile;

import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.extendable.LinkedFileModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The type Resume linked file.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RESUME_LINKED_FILE")
public class ResumeLinkedFile extends LinkedFileModel<Long> implements ICodeAssignable, ITenantAssignable {

    @Id
    @SequenceGenerator(name = "resume_multi_file_sequence_generator", sequenceName = "resume_multi_file_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resume_multi_file_sequence_generator")
    @Setter
    private Long id;

    @Setter
    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;
}
