package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

public class LandingPageController {

    @FXML
    private VBox heroSection;

    @FXML
    public void initialize() {
        // Animation for the hero section
        FadeTransition ft = new FadeTransition(Duration.millis(1200), heroSection);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
    @FXML
    protected void handleRegisterButton() throws IOException {
        SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/RegisterView.fxml");
    }
    @FXML
    protected void handleLoginButton() throws IOException {
        SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LoginView.fxml");
    }
}