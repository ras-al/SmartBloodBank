package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import com.example.smartbloodbank.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Button donorDashboardButton;
    @FXML
    private Label welcomeLabel; // New FXML element for the top navbar
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;

        // Set the text in the new top navbar
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");

        // Show/hide the sidebar button based on role
        donorDashboardButton.setVisible("Donor".equals(user.getRole()));

        // Load the homepage by default
        handleHomeButton();
    }

    @FXML
    protected void handleHomeButton() {
        loadView("/com/example/smartbloodbank/HomePageView.fxml");
    }

    @FXML
    protected void handleDonorDashboardButton() {
        loadView("/com/example/smartbloodbank/DonorDashboardView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent subView = loader.load();

            if (fxmlFile.contains("DonorDashboardView")) {
                DonorDashboardController controller = loader.getController();
                controller.initData(currentUser);
            } else if (fxmlFile.contains("HomePageView")) {
                HomePageController controller = loader.getController();
                controller.setDashboardController(this);
                controller.initData(currentUser);
            }

            mainPane.setCenter(subView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}