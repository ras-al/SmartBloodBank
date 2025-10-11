package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.CampaignOrganizer;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.HospitalStaff;
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

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
    @FXML private VBox organizerFields; // New field
    @FXML private TextField organizationNameField; // New field

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
        } else if (user instanceof CampaignOrganizer) {
            CampaignOrganizer organizer = (CampaignOrganizer) user;
            organizationNameField.setText(organizer.getOrganizationName());
            organizerFields.setVisible(true);
            organizerFields.setManaged(true);
            roleSpecificFields.setVisible(true);
            roleSpecificFields.setManaged(true);
        }
    }

    @FXML
    protected void handleSaveChanges() {
        if (currentUser == null || currentUser.getUid() == null) {
            showAlert("Error", "Could not save changes. User not loaded correctly.");
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("username", usernameField.getText());

        if (currentUser instanceof HospitalStaff) {
            updatedData.put("hospitalName", hospitalNameField.getText());
        } else if (currentUser instanceof CampaignOrganizer) {
            updatedData.put("organizationName", organizationNameField.getText());
        }

        FirestoreService.getDb().collection("users").document(currentUser.getUid()).update(updatedData);

        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(currentUser.getUid())
                    .setDisplayName(usernameField.getText());

            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                request.setPassword(passwordField.getText());
            }

            FirebaseAuth.getInstance().updateUser(request);
            showAlert("Success", "Your profile information has been updated.");

        } catch (FirebaseAuthException e) {
            showAlert("Auth Error", "Failed to update authentication details: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}