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

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox formContainer; // The container for the login form to be animated

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Set initial state for animation (invisible and slightly down)
        formContainer.setOpacity(0);
        formContainer.setTranslateY(20);

        // Create and play the fade-in and slide-up animation
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
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = authService.login(username, password);

        if (user != null) {
            statusLabel.setText("Login Successful!");
            switchToDashboard(user);
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    private void switchToDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartbloodbank/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardController controller = loader.getController();
            controller.initData(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}