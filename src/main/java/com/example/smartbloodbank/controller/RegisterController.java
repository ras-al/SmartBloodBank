package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class RegisterController {

    // Common Fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private ChoiceBox<String> roleChoiceBox;

    // Role-Specific Containers
    @FXML private VBox donorFields;
    @FXML private VBox hospitalFields;

    // Role-Specific Input Fields
    @FXML private ComboBox<String> bloodTypeComboBox;
    @FXML private TextField hospitalNameField;

    @FXML
    public void initialize() {
        // Populate the role selection box
        roleChoiceBox.setItems(FXCollections.observableArrayList("Donor", "Hospital Staff"));

        // Add a listener to show/hide fields based on role selection
        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateForm(newVal);
        });

        // Populate blood type ComboBox
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
    }

    private void updateForm(String role) {
        // Hide all role-specific fields first
        donorFields.setVisible(false);
        donorFields.setManaged(false);
        hospitalFields.setVisible(false);
        hospitalFields.setManaged(false);

        // Show the fields for the selected role
        if ("Donor".equals(role)) {
            donorFields.setVisible(true);
            donorFields.setManaged(true);
        } else if ("Hospital Staff".equals(role)) {
            hospitalFields.setVisible(true);
            hospitalFields.setManaged(true);
        }
    }

    @FXML
    protected void handleRegisterButton() {
        // In a real app, this would save the user to a database.
        // For now, we just print the details to the console to prove it works.
        String username = usernameField.getText();
        String role = roleChoiceBox.getValue();

        System.out.println("--- New Registration ---");
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);

        if ("Donor".equals(role)) {
            System.out.println("Blood Type: " + bloodTypeComboBox.getValue());
        } else if ("Hospital Staff".equals(role)) {
            System.out.println("Hospital: " + hospitalNameField.getText());
        }
        System.out.println("------------------------");
    }

    @FXML
    protected void handleHomeButton() throws IOException {
        SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
    }
}