package org.xpenbox.notifications.service;

import java.util.List;

import com.google.firebase.messaging.Message;

public interface IPushNotificationService {

    void sendPushNotification(List<Message> messages);

    void sendPushNotification(String token, String title, String message);
}
