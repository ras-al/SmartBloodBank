package com.example.smartbloodbank.main;

import com.example.smartbloodbank.service.FirestoreService;
import com.example.smartbloodbank.service.GeminiService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Firestore and Gemini
        FirestoreService.initialize();
        GeminiService.initialize();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/example/smartbloodbank/LandingPageView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        SceneManager.initialize(stage);

        stage.setTitle("DonorLink - Smart Blood Bank");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}