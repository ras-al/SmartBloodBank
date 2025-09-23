package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

public class DonorDashboardController {

    @FXML private ListView<String> donationHistoryListView;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadDonationHistory();
        // The call to loadAnalyticsChart() has been removed
    }

    private void loadDonationHistory() {
        // Corrected dummy data to be consistent (all B-)
        donationHistoryListView.getItems().clear();
        donationHistoryListView.getItems().add("Donated 1 unit of B- on Aug 15, 2025");
        donationHistoryListView.getItems().add("Donated 1 unit of B- on Feb 10, 2025");
        donationHistoryListView.getItems().add("Donated 1 unit of B- on Sep 01, 2024");
    }

}