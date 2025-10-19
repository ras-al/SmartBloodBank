package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.PartnerHospital;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MatchingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingService.class);
    private final Firestore db = FirestoreService.getDb();

    // Blood Type Compatibility Chart
    private static final Map<String, List<String>> COMPATIBILITY_MAP = new HashMap<>();
    static {
        COMPATIBILITY_MAP.put("A+", Arrays.asList("A+", "A-", "O+", "O-"));
        COMPATIBILITY_MAP.put("A-", Arrays.asList("A-", "O-"));
        COMPATIBILITY_MAP.put("B+", Arrays.asList("B+", "B-", "O+", "O-"));
        COMPATIBILITY_MAP.put("B-", Arrays.asList("B-", "O-"));
        COMPATIBILITY_MAP.put("AB+", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        COMPATIBILITY_MAP.put("AB-", Arrays.asList("AB-", "A-", "B-", "O-"));
        COMPATIBILITY_MAP.put("O+", Arrays.asList("O+", "O-"));
        COMPATIBILITY_MAP.put("O-", Arrays.asList("O-"));
    }

    public void findBloodSource(BloodRequest request) {
        new Thread(() -> {
            try {
                LOGGER.info("Phase 1: Checking partner hospital inventory for compatible types for {}", request.getBloodType());
                int unitsFulfilled = checkAndReservePartnerInventory(request);
                int unitsStillNeeded = request.getUnitsRequired() - unitsFulfilled;

                if (unitsStillNeeded <= 0) {
                    String statusMessage = String.format("Completed: %d units sourced from partner hospitals.", unitsFulfilled);
                    updateRequestStatus(request.getRequestId(), statusMessage, unitsFulfilled);
                    LOGGER.info("Request fully fulfilled by partner hospitals.");
                    return;
                }

                if (unitsFulfilled > 0) {
                    LOGGER.info("{} units fulfilled by partners. Still need {} units.", unitsFulfilled, unitsStillNeeded);
                    updateRequestStatus(request.getRequestId(), "Partially Fulfilled - Searching Donors", unitsFulfilled);
                }

                LOGGER.info("Phase 2: Searching for voluntary donors.");
                searchForVoluntaryDonors(request, unitsStillNeeded);

            } catch (ExecutionException | InterruptedException e) {
                LOGGER.error("Error during blood source matching process", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int checkAndReservePartnerInventory(BloodRequest request) throws ExecutionException, InterruptedException {
        List<String> compatibleTypes = COMPATIBILITY_MAP.get(request.getBloodType());
        if (compatibleTypes == null) return 0;

        List<QueryDocumentSnapshot> documents = db.collection("partnerHospitals").get().get().getDocuments();
        int unitsStillNeeded = request.getUnitsRequired();
        int totalUnitsReserved = 0;

        for (QueryDocumentSnapshot document : documents) {
            if (unitsStillNeeded <= 0) break;

            PartnerHospital hospital = document.toObject(PartnerHospital.class);
            Map<String, Integer> bloodStock = hospital.getBloodStock();

            if (bloodStock != null) {
                // INTELLIGENCE: Iterate through compatible blood types
                for (String bloodType : compatibleTypes) {
                    if (bloodStock.containsKey(bloodType) && bloodStock.get(bloodType) > 0) {
                        int stockAvailable = bloodStock.get(bloodType);
                        int unitsToReserve = Math.min(stockAvailable, unitsStillNeeded);
                        int newStockLevel = stockAvailable - unitsToReserve;

                        db.collection("partnerHospitals").document(document.getId())
                                .update("bloodStock." + bloodType, newStockLevel);

                        LOGGER.info("Reserved {} units of compatible type {} from {}.", unitsToReserve, bloodType, hospital.getHospitalName());
                        unitsStillNeeded -= unitsToReserve;
                        totalUnitsReserved += unitsToReserve;

                        if (unitsStillNeeded <= 0) break;
                    }
                }
            }
        }
        return totalUnitsReserved;
    }

    private void searchForVoluntaryDonors(BloodRequest request, int unitsNeeded) throws ExecutionException, InterruptedException {
        List<String> compatibleTypes = COMPATIBILITY_MAP.get(request.getBloodType());
        if (compatibleTypes == null) return;

        Query query = db.collection("users")
                .whereEqualTo("role", "Donor")
                .whereIn("bloodType", compatibleTypes);

        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        if (documents.isEmpty()) {
            LOGGER.warn("No donors found for compatible blood types. Triggering AI campaign suggestion.");
            triggerCampaignSuggestion(request.getBloodType(), "Kollam");
            return;
        }

        LOGGER.info("Found {} potential donors with compatible blood types.", documents.size());
        for (QueryDocumentSnapshot document : documents) {
            Donor donor = document.toObject(Donor.class);
            donor.setUid(document.getId());
            sendNotificationToDonor(donor, request);
        }
    }

    private void sendNotificationToDonor(Donor donor, BloodRequest request) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("donorId", donor.getUid());
        notification.put("requestIdString", String.valueOf(request.getRequestId()));
        notification.put("message", "Urgent need for your blood type (" + request.getBloodType() + ").");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("status", "unread");

        db.collection("notifications").add(notification);
        LOGGER.info("Notification sent to donor: {}", donor.getUsername());
    }

    private void triggerCampaignSuggestion(String bloodType, String location) {
        String suggestion = GeminiService.getCampaignSuggestion(bloodType, location);

        Map<String, Object> suggestionData = new HashMap<>();
        suggestionData.put("bloodType", bloodType);
        suggestionData.put("location", location);
        suggestionData.put("suggestionText", suggestion);
        suggestionData.put("timestamp", System.currentTimeMillis());

        db.collection("campaignSuggestions").add(suggestionData);
        LOGGER.info("AI Campaign Suggestion created for {} in {}", bloodType, location);
    }

    private void updateRequestStatus(int requestId, String newStatus, int unitsFulfilled) {
        db.collection("bloodRequests").document(String.valueOf(requestId)).update(
                "status", newStatus,
                "unitsFulfilled", unitsFulfilled
        );
    }
}