package eu.isygoit.encrypt.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * The type Data.
 *
 * @param <V> the type parameter
 */
@AllArgsConstructor
@NoArgsConstructor
@lombok.Data
@SuperBuilder
public class Data<V> {
    private Date calculationDate;
    private Long durationInMs;
    private V value;
}
