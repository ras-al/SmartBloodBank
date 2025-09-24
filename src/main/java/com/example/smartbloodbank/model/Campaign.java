package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class Campaign {
    private String campaignName;
    private String location;
    private LocalDate campaignDate;
    private int goal; // e.g., target number of units

    public Campaign(String campaignName, String location, LocalDate campaignDate, int goal) {
        this.campaignName = campaignName;
        this.location = location;
        this.campaignDate = campaignDate;
        this.goal = goal;
    }

    // --- Getters and Setters ---
    public String getCampaignName() {
        return campaignName;
    }
    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public LocalDate getCampaignDate() {
        return campaignDate;
    }
    public void setCampaignDate(LocalDate campaignDate) {
        this.campaignDate = campaignDate;
    }
    public int getGoal() {
        return goal;
    }
    public void setGoal(int goal) {
        this.goal = goal;
    }

    // Optional: Override toString for easier display in ListView
    @Override
    public String toString() {
        return campaignName + " at " + location + " on " + campaignDate;
    }
}