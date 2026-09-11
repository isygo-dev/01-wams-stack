package eu.isygoit.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TutorialDto {


    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
