package eu.isygoit.dto.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Linked file dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkedFileDto<I extends Serializable> extends FileEntityDto<I> {

    private String code;
    private String originalFileName;
    private Long crc16;
    private Long crc32;
    private Long size;
    private Long version;
    private String mimetype;
}
