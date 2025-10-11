package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
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

    @FXML
    protected void handleDonorDashboardClick() {
        if (dashboardController != null) {
            dashboardController.handleDonorDashboardButton();
        }
    }

    @FXML
    protected void handleFindDriveClick() {
        // This is now a fully implemented feature
        if (dashboardController != null) {
            dashboardController.loadView("/com/example/smartbloodbank/FindDriveView.fxml");
        }
    }
}