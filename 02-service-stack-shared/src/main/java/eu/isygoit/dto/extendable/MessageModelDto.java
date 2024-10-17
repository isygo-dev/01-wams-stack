package eu.isygoit.dto.extendable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Message model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class MessageModelDto<T extends Serializable> extends AbstractAuditableDto<T> {

    private String code;
    private String locale;
    private String text;
    private String forcedText;
}
