package eu.isygoit.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * The type Request context dto.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ContextRequestDto {

    public static final String x_sender_tenant = "X-SENTER-TENANT";
    public static final String x_sender_user = "X-SENTER-USER";
    public static final String x_log_app = "X-LOG-APP";
    public static final String x_is_admin = "X-IS-ADMIN";

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
