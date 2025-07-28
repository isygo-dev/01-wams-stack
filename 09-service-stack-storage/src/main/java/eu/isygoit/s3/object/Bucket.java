package eu.isygoit.s3.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * The type Bucket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bucket {

    private String name;
    private ZonedDateTime creationDate;
}
