package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class Donor extends User {
    private String bloodType;
    private LocalDate lastDonationDate;
    private String location;

    public Donor(int userId, String username, String password, String email, String bloodType, String location) {
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
    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }
    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}