package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class DonationRecord {
    private LocalDate donationDate;
    private int units;
    private String bloodType;

    public DonationRecord(LocalDate donationDate, int units, String bloodType) {
        this.donationDate = donationDate;
        this.units = units;
        this.bloodType = bloodType;
    }

    // --- Getters ---
    public LocalDate getDonationDate() {
        return donationDate;
    }

    public int getUnits() {
        return units;
    }

    public String getBloodType() {
        return bloodType;
    }
}