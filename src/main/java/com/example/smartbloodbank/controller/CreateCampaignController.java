package com.example.smartbloodbank.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

public class CreateCampaignController {

    @FXML private TextField campaignNameField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> goalSpinner;

    @FXML
    protected void handleCreateCampaign() {
        String campaignName = campaignNameField.getText();
        String location = locationField.getText();

        if (campaignName.isEmpty() || location.isEmpty() || datePicker.getValue() == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }
        // In a real app, you would save this new Campaign object to a database.
        System.out.println("New Campaign Created: " + campaignName + " " + location + " " + datePicker.getValue() + " " + goalSpinner.getValue());
        showAlert("Success", "The new campaign has been created successfully!");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}