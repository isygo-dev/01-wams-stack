package eu.isygoit.jwt.filter;

import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Query criteria.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class QueryCriteria {

    @Builder.Default
    private IEnumCriteriaCombiner.Types combiner = IEnumCriteriaCombiner.Types.OR;
    private String name;
    private IEnumOperator.Types operator;
    private String value;
}
