package org.xpenbox.notifications.service.impl;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.notifications.service.IDeviceTokenService;
import org.xpenbox.notifications.service.IPushNotificationService;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PushNotificationService implements IPushNotificationService {
    private final static Logger LOG = Logger.getLogger(PushNotificationService.class.getName());

    private final IDeviceTokenService deviceTokenService;

    public PushNotificationService(IDeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @Override
    public void sendPushNotification(String token, String title, String body) {
        if (token == null || token.trim().isEmpty()) {
            LOG.warnf("Cannot send push notification: token is null or empty");
            return;
        }

        String cleanToken = token.trim();

        try {
            LOG.debugf("Sending push notification to token: %s", cleanToken);
            LOG.debugf("Notification title: %s", title);
            LOG.debugf("Notification body: %s", body);
            
            // Configure Android-specific options with HIGH priority for Samsung devices
            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("xpenbox_default_channel")
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .setDefaultSound(true)
                    .setDefaultVibrateTimings(true)
                    .build())
                .build();
            
            // Add data payload to ensure notifications work on Samsung devices when app is in background
            Message message = Message.builder()
                .setToken(cleanToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putData("title", title)
                .putData("body", body)
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .setAndroidConfig(androidConfig)
                .build();

            FirebaseMessaging.getInstance().send(message);
            LOG.infof("Push notification sent successfully");
        } catch (FirebaseMessagingException e) {
            LOG.errorf("Firebase error [%s]: %s", 
                e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "UNKNOWN", 
                e.getMessage());
            
            if ("UNREGISTERED".equals(e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : null)) {
                deviceTokenService.removeDeviceToken(token);
            }
        } catch (Exception e) {
            LOG.error("Error sending push notification", e);
        }
    }
    
}
