package eu.isygoit.quartz.types;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Single job data.
 *
 * @param <V> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SingleJobData<V> {

    private String key;
    private V value;
}
