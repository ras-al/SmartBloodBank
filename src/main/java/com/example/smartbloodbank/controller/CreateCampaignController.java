package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.Campaign;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.Firestore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCampaignController {

    @FXML private TextField campaignNameField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> goalSpinner;
    private String organizerId;

    public void initData(String organizerId) {
        this.organizerId = organizerId;
    }

    public void initData(String organizerId, Map<String, Object> suggestionData) {
        this.organizerId = organizerId;

        String location = (String) suggestionData.get("location");
        String suggestionText = (String) suggestionData.get("suggestionText");

        if (location != null) {
            locationField.setText(location);
        }

        if (suggestionText != null) {
            Pattern titlePattern = Pattern.compile("Title: (.*)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = titlePattern.matcher(suggestionText);
            if (matcher.find()) {
                campaignNameField.setText(matcher.group(1).trim());
            } else {
                campaignNameField.setText("Drive for " + suggestionData.get("bloodType") + " blood");
            }
        }
    }

    @FXML
    protected void handleCreateCampaign() {
        String campaignName = campaignNameField.getText();
        String location = locationField.getText();
        LocalDate campaignDate = datePicker.getValue();
        Integer goal = goalSpinner.getValue();

        if (campaignName.isEmpty() || location.isEmpty() || campaignDate == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        String campaignId = UUID.randomUUID().toString();

        Campaign newCampaign = new Campaign(campaignId, campaignName, location, campaignDate.toString(), goal);
        newCampaign.setOrganizerId(this.organizerId);

        Firestore db = FirestoreService.getDb();
        db.collection("campaigns").document(campaignId).set(newCampaign);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Campaign Created!");
        alert.setHeaderText("Your new campaign has been saved.");
        alert.setContentText("Share the public link to promote your drive:\nhttps://donorlink-app.com/campaign/" + campaignId);
        alert.showAndWait();

        campaignNameField.clear();
        locationField.clear();
        datePicker.setValue(null);
        goalSpinner.getValueFactory().setValue(50);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}