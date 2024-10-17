package eu.isygoit.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.isygoit.dto.extendable.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * The type Request context dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RequestContextDto extends AbstractDto {

    private String senderDomain;
    private String senderUser;
    private Boolean isAdmin;
    private String logApp;

    /**
     * Gets created by string.
     *
     * @return the created by string
     */
    @JsonIgnore
    public String getCreatedByString() {
        if (StringUtils.isEmpty(senderUser) || StringUtils.isEmpty(senderDomain)) {
            return "anonymousUser";
        }
        return new StringBuilder(senderUser).append("@").append(senderDomain).toString();
    }
}
