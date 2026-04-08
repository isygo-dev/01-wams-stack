package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AbstractDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * The type Next code dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NextCodeDto extends AbstractDto<Long> {

    @Setter
    private Long id;
    private String tenant;
    private String entity;
    private String attribute;
    private String prefix;
    private String suffix;
    @Builder.Default
    private Long codeValue = 0L;
    @Builder.Default
    private Long valueLength = 6L;
    @Builder.Default
    private Integer increment = 1;

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode() {
        return ((prefix != null ? prefix.trim() : "")
                + String.format("%1$" + (valueLength != null ? valueLength : 6L) + "s", (codeValue != null ? codeValue : 0L))
                + (suffix != null ? suffix.trim() : ""))
                .replace(" ", "0");
    }

    /**
     * Next code next code dto.
     *
     * @return the next code dto
     */
    public NextCodeDto nextCode() {
        codeValue += (increment != null ? increment : 1);
        return this;
    }
}
