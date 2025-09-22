package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.PartnerHospital;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class PartnerHospitalController {

    @FXML
    private ListView<PartnerHospital> hospitalListView;

    @FXML
    public void initialize() {
        // This is dummy data. In a real app, you would fetch this from a database.
        ObservableList<PartnerHospital> hospitals = FXCollections.observableArrayList(
                new PartnerHospital("Lakeshore Hospital", "Kochi, Kerala", "9876543210"),
                new PartnerHospital("Aster Medcity", "Kochi, Kerala", "9123456789"),
                new PartnerHospital("Amrita Hospital", "Kochi, Kerala", "9555123456")
        );

        hospitalListView.setItems(hospitals);
    }
}