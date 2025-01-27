package eu.isygoit.dto.extendable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.helper.BeanHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

/**
 * The type Abstract dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractDto implements IDto {

    @JsonIgnore
    private String sectionName;

    @JsonIgnore
    @Override
    public String getSectionName() {
        if (!StringUtils.hasText(this.sectionName)) {
            return this.getClass().getSimpleName().replace("Dto", "");
        } else {
            return this.sectionName;
        }
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        for (Field field : this.getClass().getDeclaredFields()) {
            Object fieldValue = BeanHelper.callGetter(this, field.getName());
            if (Objects.nonNull(fieldValue)) {
                if (IIdentifiableDto.class.isAssignableFrom(field.getType())) {
                    if (!((IIdentifiableDto) fieldValue).isEmpty()) {
                        return false;
                    }
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    if (!CollectionUtils.isEmpty((Collection) fieldValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
