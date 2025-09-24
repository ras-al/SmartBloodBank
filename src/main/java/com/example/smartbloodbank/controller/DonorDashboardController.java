package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.DonationRecord;
import com.example.smartbloodbank.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.time.LocalDate;

public class DonorDashboardController {

    @FXML private ListView<DonationRecord> donationHistoryListView;
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadDonationHistory();
    }

    private void loadDonationHistory() {
        // This is dummy data. In the future, this will be fetched from the database.
        ObservableList<DonationRecord> donationRecords = FXCollections.observableArrayList(
                new DonationRecord("REC001", LocalDate.of(2025, 8, 15), "Town Hall, Kollam", 1, "B-"),
                new DonationRecord("REC002", LocalDate.of(2025, 2, 10), "City Hospital, Kollam", 1, "B-"),
                new DonationRecord("REC003", LocalDate.of(2024, 9, 1), "Technopark, Trivandrum", 1, "B-")
        );
        donationHistoryListView.setItems(donationRecords);
    }
}