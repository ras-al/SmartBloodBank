package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.DonationRecord;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

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
        if ("Completed".equalsIgnoreCase(request.getStatus()) || request.getStatus().startsWith("Fulfilled")) {
            card.getStyleClass().add("status-fulfilled");
        } else {
            card.getStyleClass().add("status-searching");
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
        String unitsText = String.format("%d / %d", request.getUnitsFulfilled(), request.getUnitsRequired());
        Text unitsValue = new Text(unitsText);
        unitsValue.getStyleClass().add("card-detail-value");
        unitsBox.getChildren().addAll(new Text("UNITS FULFILLED"), unitsValue);

        VBox statusBox = new VBox(5);
        Text statusValue = new Text(request.getStatus());
        statusValue.getStyleClass().add("card-subtitle");
        statusBox.getChildren().addAll(new Text("STATUS"), statusValue);

        detailsBox.getChildren().addAll(bloodTypeBox, unitsBox, statusBox);

        Button fulfillButton = new Button("Record a Donation");
        fulfillButton.getStyleClass().add("primary-button");
        fulfillButton.setOnAction(event -> showFulfillDialog(documentId, request));
        if (request.getStatus().toLowerCase().contains("completed") || request.getStatus().toLowerCase().contains("fulfilled")) {
            fulfillButton.setDisable(true);
        }

        card.getChildren().addAll(title, subtitle, detailsBox, fulfillButton);
        return card;
    }

    private void showFulfillDialog(String documentId, BloodRequest request) {
        try {
            List<Donor> potentialDonors = db.collection("users")
                    .whereEqualTo("role", "Donor")
                    .whereEqualTo("bloodType", request.getBloodType())
                    .get().get().getDocuments()
                    .stream()
                    .map(doc -> {
                        Donor d = doc.toObject(Donor.class);
                        d.setUid(doc.getId());
                        return d;
                    })
                    .collect(Collectors.toList());

            if (potentialDonors.isEmpty()) {
                showAlert("Info", "No registered donors with this blood type were found.");
                return;
            }

            Dialog<Pair<Donor, Integer>> dialog = new Dialog<>();
            dialog.setTitle("Record Donation");
            dialog.setHeaderText("Select the donor and the number of units they provided.");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            ComboBox<Donor> donorComboBox = new ComboBox<>();
            donorComboBox.getItems().setAll(potentialDonors);
            donorComboBox.getSelectionModel().selectFirst();

            Spinner<Integer> unitsSpinner = new Spinner<>(1, request.getUnitsRequired() - request.getUnitsFulfilled(), 1);
            unitsSpinner.setEditable(true);

            grid.add(new Label("Select Donor:"), 0, 0);
            grid.add(donorComboBox, 1, 0);
            grid.add(new Label("Units Donated:"), 0, 1);
            grid.add(unitsSpinner, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new Pair<>(donorComboBox.getValue(), unitsSpinner.getValue());
                }
                return null;
            });

            Optional<Pair<Donor, Integer>> result = dialog.showAndWait();
            result.ifPresent(pair -> {
                recordDonation(documentId, request, pair.getKey(), pair.getValue());
            });

        } catch (InterruptedException | ExecutionException e) {
            showAlert("Error", "Could not fetch donor list: " + e.getMessage());
        }
    }

    private void recordDonation(String documentId, BloodRequest request, Donor donor, int unitsDonated) {
        int newUnitsFulfilled = request.getUnitsFulfilled() + unitsDonated;
        String newStatus;

        if (newUnitsFulfilled >= request.getUnitsRequired()) {
            newStatus = "Completed";
        } else {
            newStatus = "Partially Fulfilled";
        }

        db.collection("bloodRequests").document(documentId).update(
                "unitsFulfilled", newUnitsFulfilled,
                "status", newStatus
        );

        if ("Completed".equals(newStatus)) {
            updateNotificationsForRequest(request.getRequestId());
        }

        String recordId = UUID.randomUUID().toString();
        DonationRecord record = new DonationRecord(
                recordId,
                LocalDate.now().toString(),
                "Emergency Request",
                unitsDonated,
                donor.getBloodType()
        );
        db.collection("users").document(donor.getUid()).collection("donations").document(recordId).set(record);
        db.collection("users").document(donor.getUid()).update("badges", FieldValue.arrayUnion("Urgent Responder"));

        showAlert("Success", "Successfully recorded " + unitsDonated + " unit(s) from " + donor.getUsername() + ".");
    }

    private void updateNotificationsForRequest(int requestId) {
        // --- THIS IS THE CORRECTED METHOD ---
        // This runs the update on a background thread to avoid blocking the UI
        new Thread(() -> {
            try {
                // Perform a one-time fetch of the notification documents
                List<QueryDocumentSnapshot> documents = db.collection("notifications")
                        .whereEqualTo("requestId", requestId)
                        .get()
                        .get() // This waits for the asynchronous operation to complete
                        .getDocuments();

                // Loop through the results and update each one
                for (QueryDocumentSnapshot doc : documents) {
                    doc.getReference().update("status", "Fulfilled");
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error updating notifications: " + e.getMessage());
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}