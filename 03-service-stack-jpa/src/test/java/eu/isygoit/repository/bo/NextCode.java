package eu.isygoit.repository.bo;

import eu.isygoit.model.extendable.NextCodeModel;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public class NextCode extends NextCodeModel<Long> {

    private Long Id;
}
