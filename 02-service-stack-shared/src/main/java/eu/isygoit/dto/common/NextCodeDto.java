package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * The type Next code dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NextCodeDto extends AbstractAuditableDto<Long> {

    private String domain;
    private String entity;
    private String attribute;
    private String prefix;
    private String suffix;
    @Builder.Default
    private Long value = 0L;
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
        return ((!StringUtils.hasText(prefix) ? prefix.trim() : "")
                + String.format("%1$" + (Objects.nonNull(valueLength) ? valueLength : 6L) + "s", (Objects.nonNull(value) ? value : 0L))
                + (!StringUtils.hasText(suffix) ? suffix.trim() : ""))
                .replace(" ", "0");
    }

    /**
     * Next code next code dto.
     *
     * @return the next code dto
     */
    public NextCodeDto nextCode() {
        value += (Objects.nonNull(increment) ? increment : 1);
        return this;
    }
}
