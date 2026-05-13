package org.xpenbox.notifications.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.notifications.service.IDeviceTokenService;
import org.xpenbox.notifications.service.IPushNotificationService;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PushNotificationService implements IPushNotificationService {
    private final static Logger LOG = Logger.getLogger(PushNotificationService.class.getName());

    private final IDeviceTokenService deviceTokenService;

    public PushNotificationService(IDeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @Override
    public void sendPushNotification(String token, String title, String body) {
        LOG.infof("Sending push notification to token: %s, title: %s, message: %s", token, title, body);

        try {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .build();

            String response = FirebaseMessaging.getInstance()
                .send(message);

            LOG.infof("Successfully sent push notification: %s", response );
        } catch (FirebaseMessagingException e) {
            String errorCode = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "Unknown error code";

            LOG.debugf("FCM Error: %s", errorCode);

            if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                deviceTokenService.removeDeviceToken(token);
            } else {
                LOG.errorf("Error sending push notification: %s", e.getMessage());
            }

        } catch (Exception e) {
            LOG.errorf("Error sending push notification: %s", e.getMessage());
        }
    }
    
}
