package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.service.FirestoreService;
import com.example.smartbloodbank.service.MatchingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

import java.time.LocalDate;

public class PostRequestController {

    @FXML private ComboBox<String> bloodTypeComboBox;
    @FXML private Spinner<Integer> unitsSpinner;
    private final MatchingService matchingService = new MatchingService();

    @FXML
    public void initialize() {
        bloodTypeComboBox.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    }

    @FXML
    protected void handleSubmitRequest() {
        String bloodType = bloodTypeComboBox.getValue();
        Integer units = unitsSpinner.getValue();

        if (bloodType == null || bloodType.isEmpty()) {
            showAlert("Validation Error", "Please select a blood type.");
            return;
        }

        BloodRequest newRequest = new BloodRequest(
                (int) (System.currentTimeMillis() / 1000), // Unique ID
                bloodType,
                units,
                "Searching",
                LocalDate.now()
        );

        FirestoreService.getDb().collection("bloodRequests").document(String.valueOf(newRequest.getRequestId())).set(newRequest);
        matchingService.findBloodSource(newRequest);

        showAlert("Request Submitted", "Your urgent request has been posted. The system is now searching for a match.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}