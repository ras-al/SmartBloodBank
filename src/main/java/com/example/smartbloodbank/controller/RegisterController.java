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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class RegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private TextField passwordTextField, confirmPasswordTextField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label statusLabel;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private VBox donorFields, hospitalFields, organizerFields;
    @FXML private ComboBox<String> bloodTypeComboBox;
    @FXML private TextField hospitalNameField, organizationNameField;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList("Donor", "HospitalStaff", "CampaignOrganizer"));
        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateForm(newVal));
        bloodTypeComboBox.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        statusLabel.setText("");

        // Bind visibility of password fields to the checkbox
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
        donorFields.setVisible("Donor".equals(role));
        donorFields.setManaged("Donor".equals(role));
        hospitalFields.setVisible("Hospital Staff".equals(role));
        hospitalFields.setManaged("Hospital Staff".equals(role));
        organizerFields.setVisible("Campaign Organizer".equals(role));
        organizerFields.setManaged("Campaign Organizer".equals(role));
    }

    @FXML
    protected void handleRegisterButton() {
        try {
            if (!validateInputs()) {
                return; // Stop if validation fails
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("username", usernameField.getText());
            userData.put("email", emailField.getText());
            userData.put("password", passwordField.getText());
            userData.put("role", roleChoiceBox.getValue());

            String role = roleChoiceBox.getValue();
            if ("Donor".equals(role)) userData.put("bloodType", bloodTypeComboBox.getValue());
            else if ("HospitalStaff".equals(role)) userData.put("hospitalName", hospitalNameField.getText());
            else if ("CampaignOrganizer".equals(role)) userData.put("organizationName", organizationNameField.getText());

            authService.registerUser(userData);
            showAlert("Success", "Registration successful! You can now log in.");
            handleHomeButton();

        } catch (FirebaseAuthException e) {
            LOGGER.error("Firebase Auth registration failed", e);
            statusLabel.setText("Error: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            statusLabel.setText("Error: Could not verify username. Please try again.");
            LOGGER.error("Error checking for existing username", e);
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

        if (email.isBlank() || username.isBlank() || password.isBlank() || role == null) {
            statusLabel.setText("Error: All fields, including role, are required.");
            return false;
        }
        if (authService.usernameExists(username)) {
            statusLabel.setText("Error: This username is already taken.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Error: Please enter a valid email address.");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            statusLabel.setText("Password must be 8+ characters with uppercase, number, and special character.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Error: Passwords do not match.");
            return false;
        }

        statusLabel.setText(""); // Clear errors if all checks pass
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