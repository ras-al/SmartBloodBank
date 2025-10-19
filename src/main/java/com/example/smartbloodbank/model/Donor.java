package com.example.smartbloodbank.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Donor extends User {
    private String bloodType;
    private String lastDonationDate;
    private String location;

    private List<String> badges;

    public Donor() {
        super();
        this.badges = new ArrayList<>();
    }

    public Donor(String userId, String username, String password, String email, String bloodType, String location) {
        super(userId, username, password, email, "Donor");
        this.bloodType = bloodType;
        this.location = location;
        this.badges = new ArrayList<>();
    }

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

    public List<String> getBadges() {
        return badges;
    }
    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}