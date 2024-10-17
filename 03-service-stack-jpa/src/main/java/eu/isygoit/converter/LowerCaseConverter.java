package eu.isygoit.converter;

import jakarta.persistence.AttributeConverter;
import org.springframework.util.StringUtils;

/**
 * The type Lower case converter.
 */
public class LowerCaseConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String var) {
        if (!StringUtils.hasText(var)) return null;
        return var.toLowerCase();
    }

    @Override
    public String convertToEntityAttribute(String var) {
        //if (!StringUtils.hasText(var)) return null;
        //return var.toLowerCase();
        return var;
    }
}
