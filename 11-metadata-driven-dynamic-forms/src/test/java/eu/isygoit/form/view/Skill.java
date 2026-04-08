package eu.isygoit.form.view;

import eu.isygoit.form.annotation.FormField;
import eu.isygoit.form.domain.FieldType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Skill {

    @FormField(label = "Skill Name", required = true)
    @NotBlank
    private String name;

    @FormField(label = "Proficiency Level", type = FieldType.SELECT)
    private String level;

    @FormField(label = "Years of Experience", type = FieldType.INTEGER)
    private Integer years;
}