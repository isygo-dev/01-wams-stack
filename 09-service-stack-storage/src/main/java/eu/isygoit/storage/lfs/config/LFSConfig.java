package eu.isygoit.storage.lfs.config;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.storage.s3.config.S3Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * The type Storage config dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LFSConfig {

    private String tenant;
    @Builder.Default
    private final IEnumStorage.Types type = IEnumStorage.Types.LAKEFS_STORAGE;
    private String userName;
    private String password;
    private String url;

    private S3Config s3Config;
}
