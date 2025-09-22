package com.example.smartbloodbank.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;

    private SceneManager(Stage stage) {
        this.stage = stage;
    }

    public static void initialize(Stage stage) {
        if (instance == null) {
            instance = new SceneManager(stage);
        }
    }

    public static SceneManager getInstance() {
        return instance;
    }

    public void switchToScene(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        stage.getScene().setRoot(root);
    }
}