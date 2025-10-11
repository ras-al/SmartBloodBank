package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.DonationRecord;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.FieldValue; // CORRECT IMPORT ADDED
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RequestStatusController {

    @FXML private FlowPane requestFlowPane;
    private final Firestore db = FirestoreService.getDb();

    @FXML
    public void initialize() {
        db.collection("bloodRequests").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            List<Node> requestCards = new ArrayList<>();
            if (snapshots != null) {
                snapshots.forEach(doc -> {
                    BloodRequest request = doc.toObject(BloodRequest.class);
                    requestCards.add(createRequestCard(request, doc.getId()));
                });
            }

            Platform.runLater(() -> {
                requestFlowPane.getChildren().clear();
                requestFlowPane.getChildren().addAll(requestCards);
            });
        });
    }

    private Node createRequestCard(BloodRequest request, String documentId) {
        VBox card = new VBox(15);
        card.getStyleClass().add("request-card");
        if ("Searching".equalsIgnoreCase(request.getStatus()) || request.getStatus().startsWith("Partially")) {
            card.getStyleClass().add("status-searching");
        } else {
            card.getStyleClass().add("status-fulfilled");
        }

        Text title = new Text("Urgent Request");
        title.getStyleClass().add("card-title");
        Text subtitle = new Text("Posted on: " + request.getPostedDate());
        subtitle.getStyleClass().add("card-subtitle");

        HBox detailsBox = new HBox(40);
        VBox bloodTypeBox = new VBox(5);
        Text bloodTypeValue = new Text(request.getBloodType());
        bloodTypeValue.getStyleClass().add("card-detail-value");
        bloodTypeBox.getChildren().addAll(new Text("BLOOD TYPE"), bloodTypeValue);

        VBox unitsBox = new VBox(5);
        Text unitsValue = new Text(String.valueOf(request.getUnitsRequired()));
        unitsValue.getStyleClass().add("card-detail-value");
        unitsBox.getChildren().addAll(new Text("UNITS NEEDED"), unitsValue);

        VBox statusBox = new VBox(5);
        Text statusValue = new Text(request.getStatus());
        statusValue.getStyleClass().add("card-subtitle");
        statusBox.getChildren().addAll(new Text("STATUS"), statusValue);

        detailsBox.getChildren().addAll(bloodTypeBox, unitsBox, statusBox);

        Button updateButton = new Button("Mark as Fulfilled");
        updateButton.getStyleClass().add("primary-button");
        updateButton.setOnAction(event -> handleUpdateRequest(documentId, request.getBloodType()));
        if (request.getStatus().toLowerCase().contains("fulfilled")) {
            updateButton.setDisable(true);
        }

        card.getChildren().addAll(title, subtitle, detailsBox, updateButton);
        return card;
    }

    private void handleUpdateRequest(String documentId, String bloodType) {
        try {
            List<Donor> potentialDonors = db.collection("users")
                    .whereEqualTo("role", "Donor")
                    .whereEqualTo("bloodType", bloodType)
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Donor d = doc.toObject(Donor.class);
                        d.setUid(doc.getId());
                        return d;
                    })
                    .collect(Collectors.toList());

            if (potentialDonors.isEmpty()) {
                showAlert("Info", "No registered donors found for this blood type to assign the donation to.");
                return;
            }

            ChoiceDialog<Donor> dialog = new ChoiceDialog<>(potentialDonors.get(0), potentialDonors);
            dialog.setTitle("Update Request");
            dialog.setHeaderText("Select the donor who fulfilled this request.\nThis will update their history and award them a badge.");
            dialog.setContentText("Select Donor:");

            Optional<Donor> result = dialog.showAndWait();
            result.ifPresent(donor -> updateRequestToFulfilled(documentId, donor));

        } catch (InterruptedException | ExecutionException e) {
            showAlert("Error", "Could not fetch donor list: " + e.getMessage());
        }
    }

    private void updateRequestToFulfilled(String documentId, Donor donor) {
        db.collection("bloodRequests").document(documentId).update("status", "Fulfilled by " + donor.getUsername());

        String recordId = UUID.randomUUID().toString();
        DonationRecord record = new DonationRecord(
                recordId,
                LocalDate.now().toString(),
                "Emergency Request Fulfilled",
                1,
                donor.getBloodType()
        );
        db.collection("users").document(donor.getUid()).collection("donations").document(recordId).set(record);

        // --- THIS IS THE CORRECTED LINE ---
        db.collection("users").document(donor.getUid()).update("badges", FieldValue.arrayUnion("Urgent Responder"));

        showAlert("Success", "Request updated and donation recorded for " + donor.getUsername() + "!");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}