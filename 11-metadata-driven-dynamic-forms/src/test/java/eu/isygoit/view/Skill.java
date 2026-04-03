package eu.isygoit.view;

import eu.isygoit.annotation.FormField;
import eu.isygoit.domain.FieldType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Skill {

    @FormField(label = "Skill Name", required = true)
    @NotBlank
    private String name;

    @FormField(label = "Proficiency Level", type = FieldType.SELECT)
    private String level;

    @FormField(label = "Years of Experience", type = FieldType.INTEGER)
    private Integer years;
}