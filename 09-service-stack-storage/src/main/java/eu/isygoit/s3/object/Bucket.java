package eu.isygoit.s3.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

/**
 * The type Bucket.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Bucket {

    private String name;
    private ZonedDateTime creationDate;
}
