package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.DonationRecord;
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DonorDashboardController {

    @FXML private ListView<DonationRecord> donationHistoryListView;
    @FXML private Text donationsCountText;
    @FXML private Text livesSavedText;
    @FXML private Text eligibilityStatusText;
    @FXML private Text nextEligibleDateText;
    @FXML private Text achievementText;
    @FXML private Text achievementDescription;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadDonationHistoryAndStats();
        loadGamificationData();
    }

    private void loadDonationHistoryAndStats() {
        if (currentUser == null || currentUser.getUid() == null) return;

        ObservableList<DonationRecord> donationRecords = FXCollections.observableArrayList();
        FirestoreService.getDb().collection("users").document(currentUser.getUid()).collection("donations")
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .get()
                .addListener(() -> {
                    try {
                        List<QueryDocumentSnapshot> documents = FirestoreService.getDb().collection("users")
                                .document(currentUser.getUid()).collection("donations").get().get().getDocuments();

                        for (QueryDocumentSnapshot document : documents) {
                            donationRecords.add(document.toObject(DonationRecord.class));
                        }

                        // Update UI on the JavaFX application thread
                        Platform.runLater(() -> {
                            donationHistoryListView.setItems(donationRecords);

                            // Calculate and display stats
                            int donationCount = donationRecords.size();
                            donationsCountText.setText(String.valueOf(donationCount));
                            livesSavedText.setText(String.valueOf(donationCount * 3)); // 1 donation can save 3 lives

                            // Calculate and display eligibility
                            if (!donationRecords.isEmpty()) {
                                DonationRecord lastDonation = donationRecords.get(0);
                                LocalDate lastDonationDate = LocalDate.parse(lastDonation.getDonationDate());
                                LocalDate nextEligibleDate = lastDonationDate.plusMonths(3); // Assuming 3 month wait period

                                if (LocalDate.now().isAfter(nextEligibleDate)) {
                                    eligibilityStatusText.setText("You are eligible to donate!");
                                    eligibilityStatusText.setStyle("-fx-fill: #27ae60;"); // Green color
                                    nextEligibleDateText.setText("");
                                } else {
                                    eligibilityStatusText.setText("Not yet eligible.");
                                    eligibilityStatusText.setStyle("-fx-fill: #e53935;"); // Red color
                                    nextEligibleDateText.setText("Next eligible date: " + nextEligibleDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                                }
                            } else {
                                eligibilityStatusText.setText("You are eligible to donate!");
                                eligibilityStatusText.setStyle("-fx-fill: #27ae60;");
                                nextEligibleDateText.setText("Your first donation can save lives.");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Platform::runLater);
    }

    private void loadGamificationData() {
        FirestoreService.getDb().collection("users").document(currentUser.getUid())
                .get()
                .addListener(() -> {
                    try {
                        DocumentSnapshot snapshot = FirestoreService.getDb().collection("users").document(currentUser.getUid()).get().get();
                        if (snapshot.exists()) {
                            List<String> badges = (List<String>) snapshot.get("badges");
                            if (badges != null && !badges.isEmpty()) {
                                Platform.runLater(() -> {
                                    achievementText.setText(String.join(", ", badges));
                                    achievementDescription.setText("Awarded for critical help and consistent donations.");
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Platform::runLater);
    }
}