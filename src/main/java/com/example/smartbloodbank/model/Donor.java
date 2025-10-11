package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class Donor extends User {
    private String bloodType;
    private String lastDonationDate; // Should be String to match our other models
    private String location;

    public Donor() {
        super();
    }

    public Donor(String userId, String username, String password, String email, String bloodType, String location) {
        super(userId, username, password, email, "Donor");
        this.bloodType = bloodType;
        this.location = location;
    }

    // --- Getters and Setters for donor-specific fields ---
    public String getBloodType() {
        return bloodType;
    }
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    public String getLastDonationDate() {
        return lastDonationDate;
    }
    public void setLastDonationDate(String lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}