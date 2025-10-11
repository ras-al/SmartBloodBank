package com.example.smartbloodbank.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DonationRecord {
    private String recordId;
    private String donationDate; // Changed to String
    private String location;
    private int unitsDonated;
    private String bloodType;

    // No-argument constructor for Firestore
    public DonationRecord() {}

    // Constructor updated to accept a String for the date
    public DonationRecord(String recordId, String donationDate, String location, int unitsDonated, String bloodType) {
        this.recordId = recordId;
        this.donationDate = donationDate;
        this.location = location;
        this.unitsDonated = unitsDonated;
        this.bloodType = bloodType;
    }

    // --- Getters ---
    public String getRecordId() { return recordId; }
    public String getDonationDate() { return donationDate; }
    public String getLocation() { return location; }
    public int getUnitsDonated() { return unitsDonated; }
    public String getBloodType() { return bloodType; }

    // --- Setters (for Firestore deserialization) ---
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public void setDonationDate(String donationDate) { this.donationDate = donationDate; }
    public void setLocation(String location) { this.location = location; }
    public void setUnitsDonated(int unitsDonated) { this.unitsDonated = unitsDonated; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    @Override
    public String toString() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            // Parse the string date before formatting it for display
            LocalDate date = LocalDate.parse(donationDate);
            return String.format("Donated %d unit(s) of %s on %s at %s",
                    unitsDonated,
                    bloodType,
                    date.format(formatter),
                    location
            );
        } catch (Exception e) {
            // Fallback in case the date string is malformed
            return String.format("Donated %d unit(s) of %s on %s at %s",
                    unitsDonated,
                    bloodType,
                    donationDate,
                    location
            );
        }
    }
}