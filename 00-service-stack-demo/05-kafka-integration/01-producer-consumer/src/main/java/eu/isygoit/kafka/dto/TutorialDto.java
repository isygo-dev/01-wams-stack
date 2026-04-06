package eu.isygoit.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TutorialDto {

    @Setter
    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
