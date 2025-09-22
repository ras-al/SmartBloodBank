package com.example.smartbloodbank.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the initial landing page
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/example/smartbloodbank/LandingPageView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Initialize the SceneManager with the primary stage
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