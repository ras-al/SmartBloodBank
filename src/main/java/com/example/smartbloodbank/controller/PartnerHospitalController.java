package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.PartnerHospital;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Map; // This is the line you need to add

public class PartnerHospitalController {

    @FXML
    private ListView<PartnerHospital> hospitalListView;

    @FXML
    public void initialize() {
        // Create dummy data with stock levels
        PartnerHospital lakeshore = new PartnerHospital("Lakeshore Hospital", "Kochi, Kerala", "9876543210");
        lakeshore.addStock("A+", 5);
        lakeshore.addStock("B-", 2);
        lakeshore.addStock("O+", 8);

        PartnerHospital aster = new PartnerHospital("Aster Medcity", "Kochi, Kerala", "9123456789");
        aster.addStock("AB+", 3);
        aster.addStock("O-", 1);

        PartnerHospital amrita = new PartnerHospital("Amrita Hospital", "Kochi, Kerala", "9555123456");
        amrita.addStock("A+", 12);
        amrita.addStock("B+", 7);
        amrita.addStock("O+", 20);


        ObservableList<PartnerHospital> hospitals = FXCollections.observableArrayList(lakeshore, aster, amrita);
        hospitalListView.setItems(hospitals);

        // --- Custom Cell Factory for Rich Display ---
        hospitalListView.setCellFactory(param -> new ListCell<>() {
            private final VBox container = new VBox();
            private final Text hospitalName = new Text();
            private final Text hospitalAddress = new Text();
            private final HBox stockBox = new HBox();

            {
                // Styles for the text elements
                hospitalName.getStyleClass().add("hospital-name");
                hospitalAddress.getStyleClass().add("hospital-address");
                stockBox.setSpacing(10);
                container.getChildren().addAll(hospitalName, hospitalAddress, stockBox);
                container.setPadding(new Insets(10, 15, 10, 15));
            }

            @Override
            protected void updateItem(PartnerHospital item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    hospitalName.setText(item.getHospitalName());
                    hospitalAddress.setText(item.getAddress());

                    stockBox.getChildren().clear(); // Clear old stock
                    // Create a label for each blood type in stock
                    for (Map.Entry<String, Integer> entry : item.getBloodStock().entrySet()) {
                        VBox stockItem = new VBox();
                        Text bloodType = new Text(entry.getKey());
                        Text units = new Text(entry.getValue() + " units");
                        bloodType.getStyleClass().add("stock-blood-type");
                        units.getStyleClass().add("stock-units");
                        stockItem.getChildren().addAll(bloodType, units);
                        stockItem.getStyleClass().add("stock-item-card");
                        stockBox.getChildren().add(stockItem);
                    }
                    setGraphic(container);
                }
            }
        });
    }
}