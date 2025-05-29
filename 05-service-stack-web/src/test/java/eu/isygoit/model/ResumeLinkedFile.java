package eu.isygoit.model;

import eu.isygoit.model.extendable.LinkedFileModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

/**
 * The type Resume linked file.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeLinkedFile extends LinkedFileModel<Long> implements IDomainAssignable {

    private Long id;
    private String domain;
}
