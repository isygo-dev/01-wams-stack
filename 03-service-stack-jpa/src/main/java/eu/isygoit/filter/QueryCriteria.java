package eu.isygoit.filter;

import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * The type Query criteria.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class QueryCriteria {

    @Setter
    @Builder.Default
    private IEnumCriteriaCombiner.Types combiner = IEnumCriteriaCombiner.Types.OR;
    private String name;
    private IEnumOperator.Types operator;
    private String value;
}
