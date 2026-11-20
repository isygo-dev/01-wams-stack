package eu.isygoit.model;

import eu.isygoit.model.extendable.FileModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * The type Resume file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResumeFile extends FileModel<Long> implements IFileEntity {


    private Long id;
    private Map<String, String> tags;
}
