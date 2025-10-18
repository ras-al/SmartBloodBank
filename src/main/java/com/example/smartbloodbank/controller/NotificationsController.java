package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationsController {

    @FXML
    private VBox notificationsVBox;
    private User currentUser;
    private final Firestore db = FirestoreService.getDb();

    public void initData(User user) {
        this.currentUser = user;
        loadNotifications();
    }

    private void loadNotifications() {
        if (currentUser == null) return;

        db.collection("notifications")
                .whereEqualTo("donorId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }

                    List<Node> notificationCards = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            notificationCards.add(createNotificationCard(doc));
                        }
                    }

                    Platform.runLater(() -> {
                        notificationsVBox.getChildren().clear();
                        notificationsVBox.getChildren().addAll(notificationCards);
                    });
                });
    }

    private Node createNotificationCard(QueryDocumentSnapshot doc) {
        HBox card = new HBox(15);
        card.getStyleClass().add("notification-card");
        card.setAlignment(Pos.CENTER);

        String message = doc.getString("message");
        Long timestamp = doc.getLong("timestamp");
        String status = doc.getString("status");

        if ("Fulfilled".equalsIgnoreCase(status) || "read".equalsIgnoreCase(status)) {
            card.setStyle("-fx-border-color: #27ae60; -fx-opacity: 0.6;"); // Green border and faded
        }

        VBox messageContainer = new VBox(5);
        Text title = new Text("Urgent Request");
        title.getStyleClass().add("notification-title");

        String subtitleText = message;
        if ("Fulfilled".equalsIgnoreCase(status)) {
            subtitleText = "[COMPLETED] " + message;
        } else if ("read".equalsIgnoreCase(status)) {
            subtitleText = "[Read] " + message;
        }
        Text subtitle = new Text(subtitleText);
        subtitle.getStyleClass().add("notification-subtitle");

        messageContainer.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text time = new Text(formatTimestamp(timestamp));
        time.getStyleClass().add("notification-time");

        card.getChildren().addAll(messageContainer, spacer, time);
        return card;
    }

    @FXML
    protected void handleMarkAllAsRead() {
        if (currentUser == null) return;

        // Run this database operation on a background thread
        new Thread(() -> {
            try {
                List<QueryDocumentSnapshot> documents = db.collection("notifications")
                        .whereEqualTo("donorId", currentUser.getUid())
                        .whereEqualTo("status", "unread")
                        .get().get().getDocuments();

                for (QueryDocumentSnapshot doc : documents) {
                    doc.getReference().update("status", "read");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
    }
}