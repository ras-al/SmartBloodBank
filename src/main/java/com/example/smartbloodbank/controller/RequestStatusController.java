package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.BloodRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RequestStatusController {

    @FXML
    private ListView<BloodRequest> requestListView;

    @FXML
    public void initialize() {
        // This is dummy data. In the future, this will be fetched from the database.
        ObservableList<BloodRequest> requests = FXCollections.observableArrayList(
                new BloodRequest(101, "B-", 2, "Searching", LocalDate.now()),
                new BloodRequest(102, "O+", 4, "Partially Fulfilled (2/4)", LocalDate.now().minusDays(1)),
                new BloodRequest(103, "AB-", 1, "Fulfilled", LocalDate.now().minusDays(2))
        );

        requestListView.setItems(requests);

        // Customize how each item in the list is displayed
        requestListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BloodRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    setText(String.format("Request #%d: %d units of %s | Status: %s | Posted: %s",
                            item.getRequestId(),
                            item.getUnitsRequired(),
                            item.getBloodType(),
                            item.getStatus(),
                            item.getPostedDate().format(formatter)
                    ));
                }
            }
        });
    }
}