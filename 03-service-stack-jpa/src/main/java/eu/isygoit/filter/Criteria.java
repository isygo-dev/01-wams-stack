package eu.isygoit.filter;

import eu.isygoit.enums.IEnumCombiner;
import eu.isygoit.enums.IEnumOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Criteria.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Criteria {

    @Builder.Default
    private IEnumCombiner.Types combiner = IEnumCombiner.Types.OR;
    private String name;
    private IEnumOperator.Types operator;
    private String value;
}
