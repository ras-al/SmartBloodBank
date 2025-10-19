package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.model.DonationRecord;
// Removed unused Donor import
import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.service.FirestoreService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DonorDashboardController {

    @FXML private ListView<DonationRecord> donationHistoryListView;
    @FXML private Text donationsCountText;
    @FXML private Text livesSavedText;
    @FXML private Text eligibilityStatusText;
    @FXML private Text nextEligibleDateText;
    @FXML private Text achievementText;
    @FXML private Text achievementDescription;

    private User currentUser;
    private final Firestore db = FirestoreService.getDb();

    private static final int ELIGIBILITY_PERIOD_MONTHS = 3;


    public void initData(User user) {
        this.currentUser = user;
        loadDonationHistoryAndStats();
        loadGamificationData();
    }

    private void loadDonationHistoryAndStats() {
        if (currentUser == null || currentUser.getUid() == null) {
            System.err.println("Cannot load dashboard data: current user or UID is null.");
            return;
        }

        final ObservableList<DonationRecord> donationRecords = FXCollections.observableArrayList();

        db.collection("users").document(currentUser.getUid()).collection("donations")
                .orderBy("donationDate", Query.Direction.DESCENDING)
                .get()
                .addListener(() -> {
                    List<DonationRecord> fetchedRecords = new ArrayList<>();
                    String lastDonationDateStr = null;

                    try {
                        List<QueryDocumentSnapshot> documents = db.collection("users")
                                .document(currentUser.getUid()).collection("donations")
                                .orderBy("donationDate", Query.Direction.DESCENDING)
                                .get().get().getDocuments();

                        for (QueryDocumentSnapshot document : documents) {
                            try {
                                fetchedRecords.add(document.toObject(DonationRecord.class));
                            } catch (Exception e) {
                                System.err.println("Error converting donation record: " + document.getId() + ", Error: " + e.getMessage());
                            }
                        }

                        DocumentSnapshot userDoc = db.collection("users").document(currentUser.getUid()).get().get();
                        if (userDoc.exists() && userDoc.contains("lastDonationDate")) {
                            lastDonationDateStr = userDoc.getString("lastDonationDate");
                        }

                        final String finalLastDonationDateStr = lastDonationDateStr;

                        Platform.runLater(() -> {
                            donationRecords.setAll(fetchedRecords);
                            donationHistoryListView.setItems(donationRecords);

                            int donationCount = donationRecords.size();
                            donationsCountText.setText(String.valueOf(donationCount));
                            livesSavedText.setText(String.valueOf(donationCount * 3));

                            boolean eligible = true;
                            LocalDate nextEligibleDate = null;
                            String eligibilityError = null;

                            if (finalLastDonationDateStr != null && !finalLastDonationDateStr.isEmpty()) {
                                try {
                                    LocalDate lastDonationDate = LocalDate.parse(finalLastDonationDateStr);
                                    nextEligibleDate = lastDonationDate.plusMonths(ELIGIBILITY_PERIOD_MONTHS);
                                    eligible = !LocalDate.now().isBefore(nextEligibleDate);
                                } catch (Exception e) {
                                    System.err.println("Error parsing lastDonationDate from profile: " + finalLastDonationDateStr + ", Error: " + e.getMessage());
                                    eligibilityError = "Eligibility Unknown (Date Error)";
                                }
                            }

                            if (eligibilityError != null) {
                                eligibilityStatusText.setText(eligibilityError);
                                eligibilityStatusText.setStyle("-fx-fill: #ffa500;");
                                nextEligibleDateText.setText("");
                            } else if (eligible) {
                                eligibilityStatusText.setText("You are eligible to donate!");
                                eligibilityStatusText.setStyle("-fx-fill: #27ae60;");
                                if (donationCount == 0) {
                                    nextEligibleDateText.setText("Your first donation can save lives.");
                                } else {
                                    nextEligibleDateText.setText("");
                                }
                            } else {
                                eligibilityStatusText.setText("Not yet eligible.");
                                eligibilityStatusText.setStyle("-fx-fill: #e53935;");
                                if (nextEligibleDate != null) {
                                    nextEligibleDateText.setText("Next eligible date: " + nextEligibleDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                                } else {
                                    nextEligibleDateText.setText("");
                                }
                            }
                        });
                    } catch (ExecutionException | InterruptedException e) {
                        System.err.println("Error fetching donation history or user profile: " + e.getMessage());
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.runLater(() -> {
                            eligibilityStatusText.setText("Error loading data.");
                            eligibilityStatusText.setStyle("-fx-fill: #e53935;");
                        });
                    } catch (Exception e) {
                        System.err.println("Unexpected error processing donation history/stats: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            eligibilityStatusText.setText("Error processing data.");
                            eligibilityStatusText.setStyle("-fx-fill: #e53935;");
                        });
                    }
                }, Platform::runLater);
    }


    private void loadGamificationData() {
        if (currentUser == null || currentUser.getUid() == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addListener(() -> {
                    List<String> badges = Collections.emptyList();
                    boolean errorOccurred = false;
                    String errorMessage = "Error loading badges."; // Default error message

                    try {
                        DocumentSnapshot snapshot = db.collection("users").document(currentUser.getUid()).get().get();
                        if (snapshot.exists()) {
                            // --- FIX: Get as Object and manually cast ---
                            Object badgesObject = snapshot.get("badges"); // Get as Object first
                            if (badgesObject instanceof List) { // Check if it's actually a List
                                List<?> badgesRaw = (List<?>) badgesObject; // Cast to List<?>
                                if (!badgesRaw.isEmpty()) {
                                    // Process the raw list
                                    badges = badgesRaw.stream()
                                            .filter(obj -> obj instanceof String) // Ensure elements are Strings
                                            .map(obj -> (String) obj)
                                            .collect(Collectors.toList());
                                }
                                // If badgesObject is not a List or badgesRaw is empty, badges remains Collections.emptyList()
                            }
                            // --- END FIX ---
                        } else {
                            System.err.println("User document not found for UID: " + currentUser.getUid());
                            errorMessage = "User profile not found.";
                            errorOccurred = true;
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        System.err.println("Error fetching user badges: " + e.getMessage());
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        errorMessage = "Database error fetching badges.";
                        errorOccurred = true;
                    } catch (Exception e) {
                        // Catch other potential errors during processing
                        System.err.println("Unexpected error loading gamification data: " + e.getMessage());
                        e.printStackTrace(); // Print stack trace for debugging
                        errorMessage = "Unexpected error processing badges.";
                        errorOccurred = true;
                    }

                    final List<String> finalBadges = badges;
                    final boolean finalErrorOccurred = errorOccurred;
                    final String finalErrorMessage = errorMessage; // Use final variable for lambda

                    Platform.runLater(() -> {
                        if(finalErrorOccurred) {
                            achievementText.setText(finalErrorMessage); // Show specific error
                            achievementText.setStyle("-fx-fill: #e53935;"); // Make error red
                            achievementDescription.setText("");
                        } else if (!finalBadges.isEmpty()) {
                            achievementText.setText(String.join(", ", finalBadges));
                            achievementText.setStyle(""); // Reset style if badges load ok
                            achievementDescription.setText("Awarded for critical help and/or consistent donations.");
                        } else {
                            achievementText.setText("No badges yet.");
                            achievementText.setStyle(""); // Reset style
                            achievementDescription.setText("Donate to earn badges!");
                        }
                    });
                }, Platform::runLater);
    }
}