package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.time.LocalDate;

public class ManageCampaignsController {

    @FXML
    private ListView<Campaign> campaignListView;

    @FXML
    public void initialize() {
        // This is dummy data. In a real app, fetch this from a database.
        ObservableList<Campaign> campaigns = FXCollections.observableArrayList(
                new Campaign("CAMP01", "Annual City Blood Drive", "Town Hall, Kollam", LocalDate.now().plusDays(30), 100),
                new Campaign("CAMP02", "University Campus Drive", "Amrita University, Kollam", LocalDate.now().plusDays(45), 75),
                new Campaign("CAMP03", "Corporate Wellness Event", "Technopark, Trivandrum", LocalDate.now().plusDays(60), 50)
        );
        campaignListView.setItems(campaigns);
    }
}