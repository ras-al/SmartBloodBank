package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiSuggestionsController {

    @FXML
    private FlowPane suggestionFlowPane;
    private DashboardController dashboardController;
    private final Firestore db = FirestoreService.getDb();

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        loadSuggestions();
    }

    private void loadSuggestions() {
        db.collection("campaignSuggestions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }

                    List<Node> suggestionCards = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Map<String, Object> data = doc.getData();
                            suggestionCards.add(createSuggestionCard(data));
                        }
                    }

                    Platform.runLater(() -> {
                        suggestionFlowPane.getChildren().clear();
                        if (suggestionCards.isEmpty()) {
                            suggestionFlowPane.getChildren().add(new Text("No AI suggestions found at this time."));
                        } else {
                            suggestionFlowPane.getChildren().addAll(suggestionCards);
                        }
                    });
                });
    }

    private Node createSuggestionCard(Map<String, Object> data) {
        VBox card = new VBox(15);
        card.getStyleClass().add("campaign-card");

        String bloodType = (String) data.get("bloodType");
        String location = (String) data.get("location");
        String suggestionText = (String) data.get("suggestionText");
        Long timestamp = (Long) data.get("timestamp");

        Text title = new Text("Low Supply Detected: " + bloodType);
        title.getStyleClass().add("card-title");

        Text locationText = new Text("In: " + location);
        locationText.getStyleClass().add("card-subtitle");

        Text suggestion = new Text(suggestionText);
        suggestion.setWrappingWidth(300); // Wrap text

        Text time = new Text("Generated: " + formatTimestamp(timestamp));
        time.getStyleClass().add("card-subtitle");

        Button createButton = new Button("Create Campaign from this");
        createButton.getStyleClass().add("primary-button");
        createButton.setOnAction(e -> {
            if (dashboardController != null) {

                dashboardController.loadView("/com/example/smartbloodbank/CreateCampaignView.fxml", data);
            }
        });

        card.getChildren().addAll(title, locationText, suggestion, time, createButton);
        return card;
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "N/A";
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
}