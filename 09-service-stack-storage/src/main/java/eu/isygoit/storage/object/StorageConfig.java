package eu.isygoit.storage.object;

import eu.isygoit.enums.IEnumStorage;
import lombok.AllArgsConstructor;
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
public class StorageConfig {

    private String tenant;
    private IEnumStorage.Types type;
    private String userName;
    private String password;
    private String url;
    private String namespace;
}
