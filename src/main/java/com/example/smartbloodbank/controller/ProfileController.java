package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.HospitalStaff;
import com.example.smartbloodbank.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private VBox roleSpecificFields;

    // Role-specific fields
    @FXML private VBox donorFields;
    @FXML private TextField bloodTypeField;
    @FXML private VBox hospitalFields;
    @FXML private TextField hospitalNameField;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;

        // Populate common fields
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());

        // Show and populate role-specific fields
        if (user instanceof Donor) {
            Donor donor = (Donor) user;
            bloodTypeField.setText(donor.getBloodType());
            donorFields.setVisible(true);
            donorFields.setManaged(true);
            roleSpecificFields.setVisible(true);
            roleSpecificFields.setManaged(true);
        } else if (user instanceof HospitalStaff) {
            HospitalStaff staff = (HospitalStaff) user;
            hospitalNameField.setText(staff.getHospitalName());
            hospitalFields.setVisible(true);
            hospitalFields.setManaged(true);
            roleSpecificFields.setVisible(true);
            roleSpecificFields.setManaged(true);
        }
    }

    @FXML
    protected void handleSaveChanges() {
        // In a real app, you would save this data back to the database
        System.out.println("Saving changes for user: " + usernameField.getText());

        showAlert("Success", "Your profile information has been updated.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}