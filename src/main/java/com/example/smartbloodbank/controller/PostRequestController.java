package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.service.InventoryService;
import com.example.smartbloodbank.service.MatchingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import java.time.LocalDate;

public class PostRequestController {

    @FXML
    private ComboBox<String> bloodTypeComboBox;
    @FXML
    private Spinner<Integer> unitsSpinner;

    // --- Service Dependencies ---
    private final InventoryService inventoryService = new InventoryService();
    private final MatchingService matchingService = new MatchingService();

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

        // --- Integration Point ---
        // 1. Check local inventory first using the InventoryService
        int availableUnits = inventoryService.checkInventory(bloodType);
        System.out.println("Inventory Service: Found " + availableUnits + " units of " + bloodType);

        // 2. If not enough, use the MatchingService to find a donor
        if (availableUnits < units) {
            System.out.println("Not enough units in stock. Engaging Matching Service...");
            BloodRequest newRequest = new BloodRequest(201, bloodType, units, "Searching", LocalDate.now());
            matchingService.findBloodSource(newRequest); // AI will search for a donor
        }

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