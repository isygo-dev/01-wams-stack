package eu.isygoit.dto.common;


import eu.isygoit.dto.extendable.AbstractDto;
import eu.isygoit.enums.IEnumNotification;
import eu.isygoit.enums.IEnumTarget;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

/**
 * The type Notification dto.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto extends AbstractDto<Long> {

    @Setter
    private Long id;
    private IEnumTarget.Types targetType;
    private IEnumNotification.Types notificationType;
    private String content;
    private String targetUrl; //url
    private Long companyId;
    private Long userTo;
    private Long userFrom;
    private String userFromName;
    private Long objectId;
    private Date noteDate;
    @Builder.Default
    private Boolean isRead = false;
    private List<String> role;
}
