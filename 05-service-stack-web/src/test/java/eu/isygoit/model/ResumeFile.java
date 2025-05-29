package eu.isygoit.model;

import eu.isygoit.model.extendable.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * The type Resume file.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeFile extends FileEntity<Long> implements IFileEntity {

    private Long id;
    private List<String> tags;
}
