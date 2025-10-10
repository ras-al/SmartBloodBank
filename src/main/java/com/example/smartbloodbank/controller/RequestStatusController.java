package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.service.FirestoreService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;

public class RequestStatusController {

    @FXML private ListView<BloodRequest> requestListView;

    @FXML
    public void initialize() {
        FirestoreService.getDb().collection("bloodRequests").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            ObservableList<BloodRequest> requests = FXCollections.observableArrayList();
            snapshots.forEach(doc -> requests.add(doc.toObject(BloodRequest.class)));

            Platform.runLater(() -> requestListView.setItems(requests));
        });

        requestListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BloodRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    setText(String.format("Request #%d: %d units of %s | Status: %s | Posted: %s",
                            item.getRequestId(), item.getUnitsRequired(), item.getBloodType(),
                            item.getStatus(), item.getPostedDate().format(formatter)));
                }
            }
        });
    }
}