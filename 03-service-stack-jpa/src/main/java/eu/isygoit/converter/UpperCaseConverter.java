package eu.isygoit.converter;

import jakarta.persistence.AttributeConverter;
import org.springframework.util.StringUtils;

/**
 * The type Upper case converter.
 */
public class UpperCaseConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String var) {
        if (!StringUtils.hasText(var)) return null;
        return var.toUpperCase();
    }

    @Override
    public String convertToEntityAttribute(String var) {
        //if (!StringUtils.hasText(var)) return null;
        //return var.toUpperCase();
        return var;
    }
}
