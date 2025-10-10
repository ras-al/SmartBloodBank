package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.AuthService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField identifierField; // Changed from emailField
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private VBox formContainer;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        formContainer.setOpacity(0);
        formContainer.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(600), formContainer);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), formContainer);
        tt.setToY(0);
        ft.play();
        tt.play();
    }

    @FXML
    protected void handleHomeButton() {
        try {
            SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
        } catch (IOException e) {
            LOGGER.error("Failed to switch to LandingPageView", e);
        }
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String identifier = identifierField.getText();
        String password = passwordField.getText();

        try {
            String uid = authService.login(identifier, password);
            if (uid != null) {
                User user = authService.getUserProfile(uid);
                if (user != null) {
                    statusLabel.setText("Login Successful!");
                    LOGGER.info("User '{}' ({}) logged in successfully.", user.getUsername(), user.getRole());
                    switchToDashboard(user);
                } else {
                    statusLabel.setText("Login successful, but profile not found.");
                    LOGGER.error("CRITICAL: User with UID {} authenticated but has no profile in Firestore.", uid);
                }
            } else {
                statusLabel.setText("Invalid username/email or password.");
                LOGGER.warn("Failed login attempt for identifier: {}", identifier);
            }
        } catch (ExecutionException | InterruptedException e) {
            statusLabel.setText("An error occurred during login.");
            LOGGER.error("Exception during login for identifier: {}", identifier, e);
            Thread.currentThread().interrupt();
        }
    }

    private void switchToDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbloodbank/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            DashboardController controller = loader.getController();
            controller.initData(user);
            Stage stage = (Stage) identifierField.getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);
        } catch (IOException e) {
            LOGGER.error("Failed to load DashboardView for user: {}", user.getUsername(), e);
        }
    }
}