package org.xpenbox.notifications.service;

public interface IPushNotificationService {

    void sendPushNotification(String token, String title, String message);
}
