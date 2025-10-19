package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import com.example.smartbloodbank.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class RegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=?])(?=\\S+$).{8,}$");

    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private TextField passwordTextField, confirmPasswordTextField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label statusLabel;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private VBox donorFields, hospitalFields, organizerFields;
    @FXML private ComboBox<String> bloodTypeComboBox;
    @FXML private TextField hospitalNameField, organizationNameField;
    @FXML private DatePicker lastDonationDatePicker;
    @FXML private TextField locationField;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList("Donor", "HospitalStaff", "CampaignOrganizer"));
        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateForm(newVal));
        bloodTypeComboBox.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        statusLabel.setText("");

        passwordTextField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordTextField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        confirmPasswordTextField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        confirmPasswordTextField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        confirmPasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        confirmPasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    private void updateForm(String role) {
        boolean isDonor = "Donor".equals(role);
        donorFields.setVisible(isDonor);
        donorFields.setManaged(isDonor);

        boolean isHospital = "HospitalStaff".equals(role);
        hospitalFields.setVisible(isHospital);
        hospitalFields.setManaged(isHospital);

        boolean isOrganizer = "CampaignOrganizer".equals(role);
        organizerFields.setVisible(isOrganizer);
        organizerFields.setManaged(isOrganizer);
    }

    @FXML
    protected void handleRegisterButton() {
        try {
            if (!validateInputs()) {
                return;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("username", usernameField.getText());
            userData.put("email", emailField.getText());
            userData.put("password", passwordField.getText());
            userData.put("role", roleChoiceBox.getValue());
            userData.put("location", locationField.getText().trim());

            String role = roleChoiceBox.getValue();
            if ("Donor".equals(role)) {
                userData.put("bloodType", bloodTypeComboBox.getValue());
                LocalDate lastDonationDate = lastDonationDatePicker.getValue();
                if (lastDonationDate != null) {
                    userData.put("lastDonationDate", lastDonationDate.toString());
                } else {
                    userData.put("lastDonationDate", "");
                }
                userData.put("badges", new ArrayList<String>());

            } else if ("HospitalStaff".equals(role)) {
                userData.put("hospitalName", hospitalNameField.getText());
            } else if ("CampaignOrganizer".equals(role)) {
                userData.put("organizationName", organizationNameField.getText());
            }

            authService.registerUser(userData);
            showAlert("Success", "Registration successful! You can now log in.");
            handleHomeButton();

        } catch (FirebaseAuthException e) {
            LOGGER.error("Firebase Auth registration failed", e);
            statusLabel.setText("Error: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            statusLabel.setText("Error: Could not verify username or register. Please try again.");
            LOGGER.error("Error during registration check/process", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOGGER.error("Failed to switch scene after registration", e);
        }
    }

    private boolean validateInputs() throws ExecutionException, InterruptedException {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleChoiceBox.getValue();
        String location = locationField.getText();

        if (email.isBlank() || username.isBlank() || password.isBlank() || role == null || location.isBlank()) {
            statusLabel.setText("Error: Email, Username, Password, Location, and Role are required.");
            return false;
        }

        if ("Donor".equals(role) && bloodTypeComboBox.getValue() == null) {
            statusLabel.setText("Error: Blood Type is required for Donors.");
            return false;
        }
        if ("HospitalStaff".equals(role) && hospitalNameField.getText().isBlank()) {
            statusLabel.setText("Error: Hospital Name is required for Hospital Staff.");
            return false;
        }
        if ("CampaignOrganizer".equals(role) && organizationNameField.getText().isBlank()) {
            statusLabel.setText("Error: Organization Name is required for Campaign Organizers.");
            return false;
        }

        // Username existence check
        if (authService.usernameExists(username)) {
            statusLabel.setText("Error: This username is already taken.");
            return false;
        }
        // Email format check
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Error: Please enter a valid email address.");
            return false;
        }
        // Password complexity check
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            statusLabel.setText("Password must be 8+ characters with uppercase, number, and special character.");
            return false;
        }
        // Password confirmation check
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Error: Passwords do not match.");
            return false;
        }

        // Date validation for donors
        if ("Donor".equals(role)) {
            LocalDate lastDonation = lastDonationDatePicker.getValue();
            if (lastDonation != null && lastDonation.isAfter(LocalDate.now())) {
                statusLabel.setText("Error: Last donation date cannot be in the future.");
                return false;
            }
        }

        statusLabel.setText("");
        return true;
    }

    @FXML
    protected void handleHomeButton() throws IOException {
        SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}