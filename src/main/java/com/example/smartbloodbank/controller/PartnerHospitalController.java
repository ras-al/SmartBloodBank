package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.PartnerHospital;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PartnerHospitalController {

    @FXML
    private FlowPane hospitalFlowPane;

    @FXML
    public void initialize() {
        loadHospitalsFromFirestore();
    }

    private void loadHospitalsFromFirestore() {
        FirestoreService.getDb().collection("partnerHospitals")
                .get()
                .addListener(() -> {
                    try {
                        List<QueryDocumentSnapshot> documents = FirestoreService.getDb()
                                .collection("partnerHospitals").get().get().getDocuments();

                        List<Node> hospitalCards = new ArrayList<>();
                        for (QueryDocumentSnapshot document : documents) {
                            PartnerHospital hospital = document.toObject(PartnerHospital.class);
                            hospitalCards.add(createHospitalCard(hospital));
                        }

                        Platform.runLater(() -> {
                            hospitalFlowPane.getChildren().clear();
                            hospitalFlowPane.getChildren().addAll(hospitalCards);
                        });

                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Error fetching partner hospitals: " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }, Platform::runLater);
    }

    private Node createHospitalCard(PartnerHospital hospital) {
        VBox card = new VBox(10); // 10px spacing
        card.getStyleClass().add("hospital-card");

        Text hospitalName = new Text(hospital.getHospitalName());
        hospitalName.getStyleClass().add("hospital-name");

        Text hospitalAddress = new Text(hospital.getAddress());
        hospitalAddress.getStyleClass().add("hospital-address");

        HBox stockBox = new HBox(10); // 10px spacing
        stockBox.setAlignment(Pos.CENTER_LEFT);

        if (hospital.getBloodStock() != null && !hospital.getBloodStock().isEmpty()) {
            for (Map.Entry<String, Integer> entry : hospital.getBloodStock().entrySet()) {
                VBox stockItem = new VBox(2); // 2px spacing
                stockItem.getStyleClass().add("stock-item-card");

                Text bloodType = new Text(entry.getKey());
                bloodType.getStyleClass().add("stock-blood-type");

                Text units = new Text(entry.getValue() + " units");
                units.getStyleClass().add("stock-units");

                stockItem.getChildren().addAll(bloodType, units);
                stockBox.getChildren().add(stockItem);
            }
        } else {
            stockBox.getChildren().add(new Text("No stock data available."));
        }

        card.getChildren().addAll(hospitalName, hospitalAddress, stockBox);
        return card;
    }
}