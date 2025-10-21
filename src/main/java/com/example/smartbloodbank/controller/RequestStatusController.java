package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.DonationRecord;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RequestStatusController {

    @FXML private FlowPane requestFlowPane;
    private final Firestore db = FirestoreService.getDb();

    private static final int ELIGIBILITY_PERIOD_MONTHS = 3;

    @FXML
    public void initialize() {
        db.collection("bloodRequests")
                .orderBy("postedDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
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

        boolean isCompleteOrCancelled = "Completed".equalsIgnoreCase(request.getStatus()) ||
                request.getStatus().startsWith("Fulfilled") ||
                "Cancelled".equalsIgnoreCase(request.getStatus());

        if (isCompleteOrCancelled) {
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

        VBox acceptedDonorsVBox = new VBox(5);
        acceptedDonorsVBox.setManaged(false);
        acceptedDonorsVBox.setVisible(false);
        if (request.getAcceptedDonors() != null && !request.getAcceptedDonors().isEmpty()) {
            Text acceptedTitle = new Text("Accepted Donors (On their way):");
            acceptedTitle.getStyleClass().add("form-label");
            acceptedDonorsVBox.getChildren().add(acceptedTitle);
            for (String donorName : request.getAcceptedDonors()) {
                Text donorText = new Text("• " + donorName);
                donorText.getStyleClass().add("accepted-donor-text");
                acceptedDonorsVBox.getChildren().add(donorText);
            }
            acceptedDonorsVBox.setManaged(true);
            acceptedDonorsVBox.setVisible(true);
        }

        Button fulfillButton = new Button("Record a Donation");
        fulfillButton.getStyleClass().add("primary-button");
        fulfillButton.setOnAction(event -> showFulfillDialog(documentId, request));

        Button cancelButton = new Button("Cancel Request");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setOnAction(event -> handleCancelRequest(documentId, request));

        HBox buttonBox = new HBox(10, fulfillButton, cancelButton);

        if (isCompleteOrCancelled) {
            fulfillButton.setDisable(true);
            cancelButton.setDisable(true);
        }

        card.getChildren().addAll(title, subtitle, detailsBox, acceptedDonorsVBox, buttonBox);
        return card;
    }

    private void handleCancelRequest(String documentId, BloodRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Request");
        alert.setHeaderText("Are you sure you want to cancel this request?");
        alert.setContentText("This action cannot be undone and will stop all donor notifications.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            db.collection("bloodRequests").document(documentId).update("status", "Cancelled");
            updateNotificationsForRequest(request.getRequestId(), "Cancelled");
        }
    }

    private boolean isDonorEligible(Donor donor) {
        String lastDonationDateStr = donor.getLastDonationDate();
        if (lastDonationDateStr == null || lastDonationDateStr.isEmpty()) {
            return true; // No donation history, so they are eligible
        }
        try {
            LocalDate lastDonationDate = LocalDate.parse(lastDonationDateStr);
            LocalDate nextEligibleDate = lastDonationDate.plusMonths(ELIGIBILITY_PERIOD_MONTHS);
            return !LocalDate.now().isBefore(nextEligibleDate);
        } catch (Exception e) {
            System.err.println("Error parsing date for donor " + donor.getUsername() + ": " + e.getMessage());
            return false;
        }
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

            List<Donor> eligibleDonors = potentialDonors.stream()
                    .filter(this::isDonorEligible)
                    .collect(Collectors.toList());

            if (eligibleDonors.isEmpty()) {
                showAlert("Info", "No *eligible* donors with blood type " + request.getBloodType() + " were found.");
                return;
            }

            Dialog<Pair<Donor, Integer>> dialog = new Dialog<>();
            dialog.setTitle("Record Donation");
            dialog.setHeaderText("Select the *eligible* donor and the number of units they provided.");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            ComboBox<Donor> donorComboBox = new ComboBox<>();
            donorComboBox.getItems().setAll(eligibleDonors);
            donorComboBox.getSelectionModel().selectFirst();

            int maxUnits = Math.max(1, request.getUnitsRequired() - request.getUnitsFulfilled());
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxUnits, 1);
            Spinner<Integer> unitsSpinner = new Spinner<>();
            unitsSpinner.setValueFactory(valueFactory);
            unitsSpinner.setEditable(true);
            unitsSpinner.setPrefWidth(donorComboBox.getPrefWidth());

            grid.add(new Label("Select Donor:"), 0, 0);
            grid.add(donorComboBox, 1, 0);
            grid.add(new Label("Units Donated:"), 0, 1);
            grid.add(unitsSpinner, 1, 1);

            dialog.getDialogPane().setContent(grid);

            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(donorComboBox.getValue() == null);
            donorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> saveButton.setDisable(newVal == null));


            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new Pair<>(donorComboBox.getValue(), unitsSpinner.getValue());
                }
                return null;
            });

            Optional<Pair<Donor, Integer>> result = dialog.showAndWait();
            result.ifPresent(pair -> {
                if (pair.getKey() != null) {
                    recordDonation(documentId, request, pair.getKey(), pair.getValue());
                }
            });

        } catch (InterruptedException | ExecutionException e) {
            showAlert("Error", "Could not fetch donor list: " + e.getMessage());
            Thread.currentThread().interrupt();
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

        Map<String, Object> updates = new HashMap<>();
        updates.put("unitsFulfilled", newUnitsFulfilled);
        updates.put("status", newStatus);
        updates.put("acceptedDonors", FieldValue.arrayRemove(donor.getUsername()));

        db.collection("bloodRequests").document(documentId).update(updates);

        if ("Completed".equals(newStatus)) {
            updateNotificationsForRequest(request.getRequestId(), "Fulfilled");
        }

        String recordId = UUID.randomUUID().toString();
        DonationRecord record = new DonationRecord(
                recordId,
                LocalDate.now().toString(),
                "Emergency Request Fulfilled",
                unitsDonated,
                donor.getBloodType()
        );
        db.collection("users").document(donor.getUid()).collection("donations").document(recordId).set(record);

        db.collection("users").document(donor.getUid()).update(
                "lastDonationDate", record.getDonationDate(),
                "badges", FieldValue.arrayUnion("Urgent Responder")
        );

        showAlert("Success", "Successfully recorded " + unitsDonated + " unit(s) from " + donor.getUsername() + ".");
    }

    private void updateNotificationsForRequest(int requestId, String newStatus) {
        new Thread(() -> {
            try {
                List<QueryDocumentSnapshot> documents = db.collection("notifications")
                        .whereEqualTo("requestIdString", String.valueOf(requestId))
                        .get()
                        .get()
                        .getDocuments();

                for (QueryDocumentSnapshot doc : documents) {
                    String currentStatus = doc.getString("status");
                    if ("unread".equalsIgnoreCase(currentStatus) || "read".equalsIgnoreCase(currentStatus) ||
                            "Cancelled".equalsIgnoreCase(newStatus) || "Fulfilled".equalsIgnoreCase(newStatus)) {
                        doc.getReference().update("status", newStatus);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error updating notifications: " + e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}