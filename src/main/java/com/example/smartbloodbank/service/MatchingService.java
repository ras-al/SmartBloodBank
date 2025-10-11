package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.BloodRequest;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.PartnerHospital;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MatchingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingService.class);
    private final Firestore db = FirestoreService.getDb();

    public void findBloodSource(BloodRequest request) {
        new Thread(() -> {
            try {
                LOGGER.info("Phase 1: Checking partner hospital inventory for {} units of {}", request.getUnitsRequired(), request.getBloodType());
                int unitsFulfilled = checkAndReservePartnerInventory(request);
                int unitsStillNeeded = request.getUnitsRequired() - unitsFulfilled;

                if (unitsStillNeeded <= 0) {
                    String statusMessage = String.format("Completed: %d units sourced from partner hospitals.", unitsFulfilled);
                    updateRequestStatus(request.getRequestId(), statusMessage, unitsFulfilled);
                    LOGGER.info("Request fully fulfilled by partner hospitals. Process stopped.");
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
        // ... (This method remains the same)
        List<QueryDocumentSnapshot> documents = db.collection("partnerHospitals").get().get().getDocuments();
        int unitsStillNeeded = request.getUnitsRequired();
        int totalUnitsReserved = 0;

        for (QueryDocumentSnapshot document : documents) {
            if (unitsStillNeeded <= 0) break;

            PartnerHospital hospital = document.toObject(PartnerHospital.class);
            Map<String, Integer> bloodStock = hospital.getBloodStock();

            if (bloodStock != null && bloodStock.containsKey(request.getBloodType())) {
                int stockAvailable = bloodStock.get(request.getBloodType());

                if (stockAvailable > 0) {
                    int unitsToReserve = Math.min(stockAvailable, unitsStillNeeded);
                    int newStockLevel = stockAvailable - unitsToReserve;

                    String documentId = document.getId();
                    db.collection("partnerHospitals").document(documentId)
                            .update("bloodStock." + request.getBloodType(), newStockLevel);

                    LOGGER.info("Reserved {} units of {} from {}. New stock level: {}",
                            unitsToReserve, request.getBloodType(), hospital.getHospitalName(), newStockLevel);

                    unitsStillNeeded -= unitsToReserve;
                    totalUnitsReserved += unitsToReserve;
                }
            }
        }
        return totalUnitsReserved;
    }

    private void searchForVoluntaryDonors(BloodRequest request, int unitsNeeded) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = db.collection("users")
                .whereEqualTo("role", "Donor")
                .whereEqualTo("bloodType", request.getBloodType())
                .get().get().getDocuments();

        if (documents.isEmpty()) {
            LOGGER.warn("No donors found for blood type {}. Triggering AI campaign suggestion.", request.getBloodType());
            triggerCampaignSuggestion(request.getBloodType(), "Kollam"); // Assuming location
            return;
        }

        LOGGER.info("Found {} potential donors for blood type {}.", documents.size(), request.getBloodType());
        for (QueryDocumentSnapshot document : documents) {
            Donor donor = document.toObject(Donor.class);
            donor.setUid(document.getId());
            sendNotificationToDonor(donor, request);
        }
    }

    private void sendNotificationToDonor(Donor donor, BloodRequest request) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("donorId", donor.getUid());
        notification.put("requestId", request.getRequestId()); // Link notification to request
        notification.put("message", "Urgent need for your blood type (" + request.getBloodType() + ").");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("status", "unread");

        db.collection("notifications").add(notification);
        LOGGER.info("Notification sent to donor: {}", donor.getUsername());
    }

    private void triggerCampaignSuggestion(String bloodType, String location) {
        String suggestion = GeminiService.getCampaignSuggestion(bloodType, location);

        // Save the suggestion for organizers to see
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