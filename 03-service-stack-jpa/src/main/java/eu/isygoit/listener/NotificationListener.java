package eu.isygoit.listener;

import eu.isygoit.model.INotifiableEntity;
import eu.isygoit.service.INotificationListenerService;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

/**
 * The type Notification listener.
 */
@Component
public class NotificationListener {

    private static INotificationListenerService notificationService;

    /**
     * Init.
     *
     * @param notificationService the notification service
     */
//@Autowired
    public void init(INotificationListenerService notificationService) {
        NotificationListener.notificationService = notificationService;
    }

    /**
     * On post load.
     *
     * @param entity the entity
     */
    @PostLoad
    void onPostLoad(INotifiableEntity entity) {
    }

    /**
     * On post persist.
     *
     * @param entity the entity
     */
    @PostPersist
    void onPostPersist(INotifiableEntity entity) {
        if (notificationService != null) {
            notificationService.performPostPersistNotification(entity);
        }
    }

    /**
     * On post remove.
     *
     * @param entity the entity
     */
    @PostRemove
    void onPostRemove(INotifiableEntity entity) {
        if (notificationService != null) {
            notificationService.performPostRemoveNotification(entity);
        }
    }

    /**
     * On post update.
     *
     * @param entity the entity
     */
    @PostUpdate
    void onPostUpdate(INotifiableEntity entity) {
        if (notificationService != null) {
            notificationService.performPostUpdateNotification(entity);
        }
    }

    /**
     * On pre persist.
     *
     * @param entity the entity
     */
    @PrePersist
    void onPrePersist(INotifiableEntity entity) {
    }

    /**
     * On pre remove.
     *
     * @param entity the entity
     */
    @PreRemove
    void onPreRemove(INotifiableEntity entity) {
    }

    /**
     * On pre update.
     *
     * @param entity the entity
     */
    @PreUpdate
    void onPreUpdate(INotifiableEntity entity) {
    }
}
