package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FindDriveController {

    @FXML
    private FlowPane driveFlowPane;

    @FXML
    public void initialize() {
        loadCampaigns();
    }

    private void loadCampaigns() {
        String todayDateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        FirestoreService.getDb().collection("campaigns")
                .whereGreaterThanOrEqualTo("campaignDate", todayDateString)
                .orderBy("campaignDate", Query.Direction.ASCENDING)
                .get()
                .addListener(() -> {
                    try {
                        List<QueryDocumentSnapshot> documents = FirestoreService.getDb().collection("campaigns")
                                .whereGreaterThanOrEqualTo("campaignDate", todayDateString)
                                .orderBy("campaignDate", Query.Direction.ASCENDING)
                                .get().get().getDocuments();

                        List<Node> campaignCards = new ArrayList<>();
                        for (QueryDocumentSnapshot document : documents) {
                            Campaign campaign = document.toObject(Campaign.class);
                            campaignCards.add(createCampaignCard(campaign));
                        }

                        Platform.runLater(() -> {
                            driveFlowPane.getChildren().clear();
                            if (campaignCards.isEmpty()) {
                                driveFlowPane.getChildren().add(new Text("No upcoming drives found."));
                            } else {
                                driveFlowPane.getChildren().addAll(campaignCards);
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error fetching upcoming campaigns: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            driveFlowPane.getChildren().clear();
                            driveFlowPane.getChildren().add(new Text("Error loading drives."));
                        });
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
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
        String formattedDate = "Unknown Date";
        try {
            LocalDate date = LocalDate.parse(campaign.getCampaignDate());
            formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            System.err.println("Could not parse campaign date: " + campaign.getCampaignDate());
            formattedDate = campaign.getCampaignDate();
        }
        dateBox.getChildren().addAll(new Text("DATE"), new Text(formattedDate));


        VBox goalBox = new VBox(5);
        Text goalValue = new Text(String.valueOf(campaign.getGoal()));
        goalValue.getStyleClass().add("card-detail-value");
        goalBox.getChildren().addAll(new Text("GOAL (UNITS)"), goalValue);

        detailsBox.getChildren().addAll(dateBox, goalBox);
        card.getChildren().addAll(title, location, detailsBox);
        return card;
    }
}