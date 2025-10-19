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
        if (dashboardController != null) {
            dashboardController.handleAiSuggestionsButton();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}