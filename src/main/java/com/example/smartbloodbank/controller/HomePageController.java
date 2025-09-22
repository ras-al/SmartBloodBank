package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class HomePageController {

    @FXML private Label welcomeLabel;
    @FXML private VBox homeRoot;
    private DashboardController dashboardController;

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

    /**
     * Handles the "Go to Dashboard" button click.
     */
    @FXML
    protected void handleDonorDashboardClick() {
        if (dashboardController != null) {
            dashboardController.handleDonorDashboardButton();
        }
    }

    /**
     * Handles the "Search Events" button click (new method).
     */
    @FXML
    protected void handleFindDriveClick() {
        // Placeholder functionality for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Coming Soon");
        alert.setHeaderText("Find a Drive");
        alert.setContentText("This feature will allow you to search for nearby donation events. Stay tuned!");
        alert.showAndWait();
    }
}