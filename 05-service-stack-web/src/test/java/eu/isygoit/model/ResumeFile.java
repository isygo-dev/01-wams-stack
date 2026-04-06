package eu.isygoit.model;

import eu.isygoit.model.extendable.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * The type Resume file.
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeFile extends FileEntity<Long> implements IFileEntity {

    @Setter
    private Long id;
    private List<String> tags;
}
