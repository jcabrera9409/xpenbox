package org.xpenbox.configuration;

import java.io.FileInputStream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirebaseConfig {
    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class);

    @ConfigProperty(name = "firebase.admin.sdk.json.path", defaultValue = "/path/to/firebase-adminsdk.json")
    private String firebaseAdminSdkJsonPath;

    @PostConstruct
    public void init() {
        LOG.info("Firebase Admin SDK JSON Path: " + firebaseAdminSdkJsonPath);
        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseAdminSdkJsonPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                LOG.info("Firebase Admin SDK initialized successfully.");
            } else {
                LOG.warn("Firebase Admin SDK is already initialized. Skipping initialization.");
            }
        }
        catch (Exception e) {
            LOG.error("Error initializing Firebase Admin SDK: " + e.getMessage(), e);
        }
    }
}
