package eu.isygoit.storage.s3.config;

import eu.isygoit.enums.IEnumStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The type S 3 config.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class S3Config {

    private String tenant;
    private IEnumStorage.Types type;
    private String userName;
    private String password;
    private String url;
    private String namespace;
    private String region;
}
