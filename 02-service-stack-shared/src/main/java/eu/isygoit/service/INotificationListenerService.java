package eu.isygoit.service;

import eu.isygoit.dto.common.NotificationDto;
import eu.isygoit.model.Notifiable;

import java.io.IOException;


/**
 * The interface Notification listener service.
 */
public interface INotificationListenerService {

    /**
     * Perform post persist notification.
     *
     * @param entity the entity
     */
    void performPostPersistNotification(Notifiable entity);

    /**
     * Perform post remove notification.
     *
     * @param entity the entity
     */
    void performPostRemoveNotification(Notifiable entity);

    /**
     * Perform post update notification.
     *
     * @param entity the entity
     */
    void performPostUpdateNotification(Notifiable entity);

    /**
     * Send notification.
     *
     * @param notificationDtos the notification dtos
     * @throws IOException the io exception
     */
    void sendNotification(NotificationDto[] notificationDtos) throws IOException;
}
