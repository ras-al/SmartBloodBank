package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ManageCampaignsController {

    @FXML
    private FlowPane campaignFlowPane;
    private String organizerId;
    private final Firestore db = FirestoreService.getDb();

    public void initData(String organizerId) {
        this.organizerId = organizerId;
        loadCampaigns();
    }

    private void loadCampaigns() {
        if (organizerId == null) return;

        // Use a real-time listener to automatically update the UI on changes
        db.collection("campaigns")
                .whereEqualTo("organizerId", organizerId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }

                    List<Node> campaignCards = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot document : snapshots) {
                            Campaign campaign = document.toObject(Campaign.class);
                            campaignCards.add(createCampaignCard(campaign));
                        }
                    }

                    Platform.runLater(() -> {
                        campaignFlowPane.getChildren().clear();
                        campaignFlowPane.getChildren().addAll(campaignCards);
                    });
                });
    }

    private Node createCampaignCard(Campaign campaign) {
        VBox card = new VBox(15);
        card.getStyleClass().add("campaign-card");

        if ("Completed".equalsIgnoreCase(campaign.getStatus())) {
            card.getStyleClass().add("campaign-completed");
        }

        Text title = new Text(campaign.getCampaignName());
        title.getStyleClass().add("card-title");

        Text location = new Text(campaign.getLocation());
        location.getStyleClass().add("card-subtitle");

        HBox detailsBox = new HBox(40);
        VBox dateBox = new VBox(5);
        dateBox.getChildren().addAll(new Text("DATE"), new Text(campaign.getCampaignDate()));

        VBox goalBox = new VBox(5);
        Text goalValue = new Text(String.valueOf(campaign.getGoal()));
        goalValue.getStyleClass().add("card-detail-value");
        goalBox.getChildren().addAll(new Text("GOAL (UNITS)"), goalValue);

        VBox statusBox = new VBox(5);
        statusBox.getChildren().addAll(new Text("STATUS"), new Text(campaign.getStatus()));

        detailsBox.getChildren().addAll(dateBox, goalBox, statusBox);

        // --- NEW BUTTON AND LOGIC ---
        Button completeButton = new Button("Mark as Complete");
        completeButton.setOnAction(event -> {
            db.collection("campaigns").document(campaign.getCampaignId()).update("status", "Completed");
        });

        if ("Completed".equalsIgnoreCase(campaign.getStatus())) {
            completeButton.setDisable(true);
        }

        card.getChildren().addAll(title, location, detailsBox, completeButton);
        return card;
    }
}