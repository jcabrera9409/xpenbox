package org.xpenbox.notifications.scheduler;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.entity.DeviceToken.Platform;
import org.xpenbox.notifications.repository.DeviceTokenRepository;
import org.xpenbox.notifications.service.IPushNotificationService;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;

/**
 * Scheduler class for managing push notification-related tasks. This class is responsible for scheduling and executing tasks related to push notifications, such as sending notifications to users at specified intervals. It uses the Quarkus Scheduler to run tasks at specified intervals, allowing for automated management of push notification delivery based on defined schedules.
 */
@Singleton
public class PushNotificationScheduler {
    private static final Logger LOG = Logger.getLogger(PushNotificationScheduler.class.getName());

    @ConfigProperty(name = "firebase.push.notification.cron")
    private String firebasePushNotificationCron;

    private final DeviceTokenRepository deviceTokenRepository;
    private final IPushNotificationService pushNotificationService;

    public PushNotificationScheduler(
        IPushNotificationService pushNotificationService,
        DeviceTokenRepository deviceTokenRepository
    ) {
        this.pushNotificationService = pushNotificationService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Scheduled(cron = "{firebase.push.notification.cron}")
    void schedulePushNotificationTask() {
        LOG.info("Running scheduled push notification task");

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByStateTrueAndPlatform(Platform.ANDROID);

        for (DeviceToken deviceToken : deviceTokens) {
            pushNotificationService.sendPushNotification(deviceToken.getToken(), "Prueba Token", "Este es un mensaje de prueba para el token: " + deviceToken.getToken());
        }

    }
    
}
