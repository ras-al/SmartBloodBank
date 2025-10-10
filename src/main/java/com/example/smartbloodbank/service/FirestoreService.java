package com.example.smartbloodbank.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirestoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreService.class);
    private static Firestore db;

    public static void initialize() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (credentialsPath == null || credentialsPath.isEmpty()) {
            LOGGER.error("FATAL ERROR: The environment variable GOOGLE_APPLICATION_CREDENTIALS is not set.");
            throw new IOException("Environment variable GOOGLE_APPLICATION_CREDENTIALS must be set.");
        }

        FileInputStream serviceAccount = new FileInputStream(credentialsPath);

        // Use the modern builder() method here
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        db = FirestoreClient.getFirestore();
        System.out.println("Firestore has been initialized.");
    }

    public static Firestore getDb() {
        return db;
    }
}