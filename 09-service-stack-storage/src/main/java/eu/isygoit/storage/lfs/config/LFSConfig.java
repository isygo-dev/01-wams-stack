package eu.isygoit.storage.lfs.config;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.storage.s3.config.S3Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The type Lfs config.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LFSConfig {

    @Builder.Default
    private final IEnumStorage.Types type = IEnumStorage.Types.LAKEFS_STORAGE;
    private String tenant;
    private String userName;
    private String password;
    private String url;
    @Builder.Default
    private String apiPrefix = "/api/v1";

    private S3Config s3Config;
}
