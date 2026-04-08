package eu.isygoit.model;

import eu.isygoit.model.extendable.LinkedFileModel;
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
public class ResumeLinkedFile extends LinkedFileModel<Long> implements ITenantAssignable {

    private Long id;
    private String tenant;
}
