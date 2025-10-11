package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.UUID;

public class CreateCampaignController {

    @FXML private TextField campaignNameField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> goalSpinner;
    private String organizerId;

    public void initData(String organizerId) {
        this.organizerId = organizerId;
    }

    @FXML
    protected void handleCreateCampaign() {
        String campaignName = campaignNameField.getText();
        String location = locationField.getText();
        LocalDate campaignDate = datePicker.getValue();
        Integer goal = goalSpinner.getValue();

        if (campaignName.isEmpty() || location.isEmpty() || campaignDate == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        String campaignId = UUID.randomUUID().toString();

        Campaign newCampaign = new Campaign(campaignId, campaignName, location, campaignDate.toString(), goal);
        newCampaign.setOrganizerId(this.organizerId);

        Firestore db = FirestoreService.getDb();
        db.collection("campaigns").document(campaignId).set(newCampaign);

        showAlert("Success", "The new campaign has been created and saved successfully!");

        campaignNameField.clear();
        locationField.clear();
        datePicker.setValue(null);
        goalSpinner.getValueFactory().setValue(50);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}