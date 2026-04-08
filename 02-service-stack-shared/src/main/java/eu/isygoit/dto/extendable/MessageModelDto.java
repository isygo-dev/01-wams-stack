package eu.isygoit.dto.extendable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Message model dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class MessageModelDto<T extends Serializable> extends AuditableDto<T> {

    private String code;
    private String locale;
    private String text;
    private String forcedText;
}
