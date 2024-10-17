package eu.isygoit.converter;

import jakarta.persistence.AttributeConverter;
import org.apache.commons.text.CaseUtils;
import org.springframework.util.StringUtils;

/**
 * The type Camel case converter.
 */
public class CamelCaseConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String var) {
        if (!StringUtils.hasText(var)) return null;
        return CaseUtils.toCamelCase(var, true);
    }

    @Override
    public String convertToEntityAttribute(String var) {
        //if (!StringUtils.hasText(var)) return null;
        //return CaseUtils.toCamelCase(var, true);
        return var;
    }
}
