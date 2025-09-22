package com.example.smartbloodbank.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

public class PostRequestController {

    @FXML
    private ComboBox<String> bloodTypeComboBox;
    @FXML
    private Spinner<Integer> unitsSpinner;

    @FXML
    public void initialize() {
        // Populate the ComboBox with blood types
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
    }

    @FXML
    protected void handleSubmitRequest() {
        String bloodType = bloodTypeComboBox.getValue();
        Integer units = unitsSpinner.getValue();

        if (bloodType == null || bloodType.isEmpty()) {
            showAlert("Validation Error", "Please select a blood type.");
            return;
        }

        // In a real application, you would save this to the database
        // and trigger the AI matching service.
        System.out.println("New Request Submitted: " + units + " units of " + bloodType);

        showAlert("Request Submitted", "Your urgent request for " + units + " units of " + bloodType + " blood has been posted. The system is now searching for a match.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}