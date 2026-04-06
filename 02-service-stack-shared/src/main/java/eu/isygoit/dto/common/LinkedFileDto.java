package eu.isygoit.dto.common;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class LinkedFileDto<T extends Serializable> extends FileEntityDto<T> {

    private String code;
    private String originalFileName;
    private Long crc16;
    private Long crc32;
    private Long size;
    private Long version;
    private String mimetype;
}
