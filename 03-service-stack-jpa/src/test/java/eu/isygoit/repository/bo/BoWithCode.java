package eu.isygoit.repository.bo;

import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public class BoWithCode implements AssignableCode, AssignableId<Long> {

    private Long id;
    private String code;
}
