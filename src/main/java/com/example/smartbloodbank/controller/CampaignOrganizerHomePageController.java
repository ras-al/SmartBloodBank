package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.example.smartbloodbank.service.GeminiService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignOrganizerHomePageController {

    @FXML private Label welcomeLabel;
    @FXML private VBox homeRoot;
    private DashboardController dashboardController;
    private final Firestore db = FirestoreService.getDb();

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void initData(User user) {
        welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
        FadeTransition ft = new FadeTransition(Duration.millis(800), homeRoot);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    protected void handleCreateCampaign() {
        if (dashboardController != null) {
            dashboardController.handleCreateCampaignButton();
        }
    }

    @FXML
    protected void handleManageCampaigns() {
        if (dashboardController != null) {
            dashboardController.handleManageCampaignsButton();
        }
    }

    @FXML
    protected void handleAIAssistant() {
        new Thread(() -> {
            try {
                // 1. Analyze blood requests to find the most needed blood type
                List<QueryDocumentSnapshot> documents = db.collection("bloodRequests").get().get().getDocuments();
                Map<String, Integer> bloodTypeCounts = new HashMap<>();
                for (QueryDocumentSnapshot doc : documents) {
                    BloodRequest request = doc.toObject(BloodRequest.class);
                    bloodTypeCounts.merge(request.getBloodType(), 1, Integer::sum);
                }

                if (bloodTypeCounts.isEmpty()) {
                    Platform.runLater(() -> showAlert("AI Assistant", "No recent request data to analyze."));
                    return;
                }

                String mostNeededBloodType = Collections.max(bloodTypeCounts.entrySet(), Map.Entry.comparingByValue()).getKey();

                // 2. Call Gemini Service to get a campaign suggestion
                String suggestion = GeminiService.getCampaignSuggestion(mostNeededBloodType, "Kollam");

                // 3. Display the suggestion in an Alert
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("AI Campaign Suggestion");
                    alert.setHeaderText("Analysis Complete: Low Supply of " + mostNeededBloodType + " Blood Detected!");
                    alert.setContentText(suggestion);
                    alert.getDialogPane().setPrefSize(480, 320); // Make dialog larger
                    alert.showAndWait();
                });

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Could not generate AI suggestion: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}