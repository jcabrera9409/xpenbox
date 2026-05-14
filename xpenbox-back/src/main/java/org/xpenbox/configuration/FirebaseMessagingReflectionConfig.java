package org.xpenbox.configuration;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Configuration class to register Firebase Messaging classes for reflection in GraalVM Native Image.
 * This is required for proper JSON serialization of Firebase messages.
 */
@RegisterForReflection(targets = {
    Message.class,
    Message.Builder.class,
    Notification.class,
    Notification.Builder.class,
    AndroidConfig.class,
    AndroidConfig.Builder.class,
    AndroidNotification.class,
    AndroidNotification.Builder.class,
    ApnsConfig.class,
    ApnsConfig.Builder.class,
    Aps.class,
    Aps.Builder.class,
    WebpushConfig.class,
    WebpushConfig.Builder.class,
    FcmOptions.class,
    FcmOptions.Builder.class
}, fields = true, methods = true)
public class FirebaseMessagingReflectionConfig {
    // This class is used only for reflection configuration
}
