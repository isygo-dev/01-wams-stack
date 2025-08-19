package eu.isygoit.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ContextRequestDto {

    private String senderTenant;
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
        if (StringUtils.isEmpty(senderUser) || StringUtils.isEmpty(senderTenant)) {
            return "anonymousUser";
        }
        return new StringBuilder(senderUser).append("@").append(senderTenant).toString();
    }
}
