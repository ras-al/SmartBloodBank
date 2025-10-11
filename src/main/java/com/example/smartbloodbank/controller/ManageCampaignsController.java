package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    public void initData(String organizerId) {
        this.organizerId = organizerId;
        loadCampaigns();
    }

    private void loadCampaigns() {
        if (organizerId == null) return;

        FirestoreService.getDb().collection("campaigns")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addListener(() -> {
                    try {
                        List<QueryDocumentSnapshot> documents = FirestoreService.getDb().collection("campaigns")
                                .whereEqualTo("organizerId", organizerId).get().get().getDocuments();

                        List<Node> campaignCards = new ArrayList<>();
                        for (QueryDocumentSnapshot document : documents) {
                            Campaign campaign = document.toObject(Campaign.class);
                            campaignCards.add(createCampaignCard(campaign));
                        }

                        Platform.runLater(() -> {
                            campaignFlowPane.getChildren().clear();
                            campaignFlowPane.getChildren().addAll(campaignCards);
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Error fetching campaigns: " + e.getMessage());
                    }
                }, Platform::runLater);
    }

    private Node createCampaignCard(Campaign campaign) {
        VBox card = new VBox(15);
        card.getStyleClass().add("campaign-card");

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

        detailsBox.getChildren().addAll(dateBox, goalBox);
        card.getChildren().addAll(title, location, detailsBox);
        return card;
    }
}