package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;

public class DonorDashboardController {

    @FXML private ListView<String> donationHistoryListView;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadDonationHistory();
    }

    private void loadDonationHistory() {
        // Dummy data for demonstration. In a real app, you would fetch this from a database.
        donationHistoryListView.getItems().add("Donated 1 unit of B- on 2025-08-15 in Kollam");
        donationHistoryListView.getItems().add("Donated 1 unit of B- on 2025-02-10 in Kollam");
        donationHistoryListView.getItems().add("Donated 1 unit of B- on 2024-09-01 in Kollam");
    }

    @FXML
    protected void handleTrackImpact() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Impact Tracker");
        alert.setHeaderText("Feature Coming Soon!");
        alert.setContentText("This feature will show how your donations have helped save lives. Thank you!");
        alert.showAndWait();
    }
}