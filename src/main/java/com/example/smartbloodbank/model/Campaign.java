package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class Campaign {
    private String campaignId;
    private String campaignName;
    private String location;
    private LocalDate campaignDate;
    private int goal; // e.g., target number of units
    private String status;

    public Campaign(String campaignId, String campaignName, String location, LocalDate campaignDate, int goal) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.location = location;
        this.campaignDate = campaignDate;
        this.goal = goal;
        this.status = "Planned"; // Default status
    }

    // --- Getters and Setters ---
    public String getCampaignId() {
        return campaignId;
    }
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }
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
    public String getStatus() {
        return status;
    }
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return campaignName + " at " + location + " on " + campaignDate;
    }
}