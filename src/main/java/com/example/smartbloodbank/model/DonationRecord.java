package com.example.smartbloodbank.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DonationRecord {
    private String recordId;
    private LocalDate donationDate;
    private String location;
    private int unitsDonated;
    private String bloodType;

    public DonationRecord(String recordId, LocalDate donationDate, String location, int unitsDonated, String bloodType) {
        this.recordId = recordId;
        this.donationDate = donationDate;
        this.location = location;
        this.unitsDonated = unitsDonated;
        this.bloodType = bloodType;
    }

    // --- Getters ---
    public String getRecordId() { return recordId; }
    public LocalDate getDonationDate() { return donationDate; }
    public String getLocation() { return location; }
    public int getUnitsDonated() { return unitsDonated; }
    public String getBloodType() { return bloodType; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return String.format("Donated %d unit(s) of %s on %s at %s",
                unitsDonated,
                bloodType,
                donationDate.format(formatter),
                location
        );
    }
}