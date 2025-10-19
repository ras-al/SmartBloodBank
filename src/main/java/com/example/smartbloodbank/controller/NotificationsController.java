package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.FieldValue;
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
import javafx.geometry.Insets;

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
        VBox cardContainer = new VBox(10);
        cardContainer.getStyleClass().add("notification-card");

        HBox mainContent = new HBox(15);
        mainContent.setAlignment(Pos.CENTER);

        String message = doc.getString("message");
        Long timestamp = doc.getLong("timestamp");
        String status = doc.getString("status");

        String statusText;
        if (status == null) status = "unread";

        switch (status.toLowerCase()) {
            case "fulfilled":
            case "completed":
                statusText = "[COMPLETED] ";
                cardContainer.setStyle("-fx-border-color: #27ae60; -fx-opacity: 0.6;");
                break;
            case "read":
                statusText = "[Read] ";
                cardContainer.setStyle("-fx-opacity: 0.7;");
                break;
            case "accepted":
                statusText = "[ACCEPTED] ";
                cardContainer.setStyle("-fx-border-color: #27ae60;");
                break;
            case "rejected":
                statusText = "[REJECTED] ";
                cardContainer.setStyle("-fx-border-color: #e53935; -fx-opacity: 0.6;");
                break;
            case "cancelled":
                statusText = "[CANCELLED] ";
                cardContainer.setStyle("-fx-border-color: #6c757d; -fx-opacity: 0.6;");
                break;
            default:
                statusText = "";
                break;
        }

        VBox messageContainer = new VBox(5);
        Text title = new Text("Urgent Request");
        title.getStyleClass().add("notification-title");

        Text subtitle = new Text(statusText + message);
        subtitle.getStyleClass().add("notification-subtitle");

        messageContainer.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text time = new Text(formatTimestamp(timestamp));
        time.getStyleClass().add("notification-time");

        mainContent.getChildren().addAll(messageContainer, spacer, time);
        cardContainer.getChildren().add(mainContent);

        if ("unread".equalsIgnoreCase(status)) {
            Button acceptButton = new Button("Accept");
            acceptButton.getStyleClass().add("accept-button");
            acceptButton.setOnAction(e -> handleAccept(doc));

            Button rejectButton = new Button("Reject");
            rejectButton.getStyleClass().add("cancel-button");
            rejectButton.setOnAction(e -> handleReject(doc));

            HBox buttonBox = new HBox(10, acceptButton, rejectButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            cardContainer.getChildren().add(buttonBox);
        }

        return cardContainer;
    }

    private void handleAccept(QueryDocumentSnapshot doc) {
        String requestIdString = doc.getString("requestIdString");
        if (requestIdString == null) {
            System.err.println("Error: Notification is missing 'requestIdString'.");
            return;
        }

        doc.getReference().update("status", "Accepted");

        db.collection("bloodRequests").document(requestIdString).update(
                "acceptedDonors", FieldValue.arrayUnion(currentUser.getUsername())
        );
    }

    private void handleReject(QueryDocumentSnapshot doc) {
        doc.getReference().update("status", "Rejected");
    }

    @FXML
    protected void handleMarkAllAsRead() {
        if (currentUser == null) return;

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