package eu.isygoit.dto.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileDto<T extends Serializable> extends FileEntityDto<T> {

    private String code;
    private String originalFileName;
    private Integer crc16;
    private Integer crc32;
    private Long size;
    private Long version;
    private String mimetype;
}
