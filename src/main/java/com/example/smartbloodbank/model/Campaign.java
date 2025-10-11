package com.example.smartbloodbank.model;

public class Campaign {
    private String campaignId;
    private String campaignName;
    private String location;
    private String campaignDate; // Changed from LocalDate to String
    private int goal; // e.g., target number of units
    private String status;
    private String organizerId;

    // No-argument constructor is required for Firestore deserialization
    public Campaign() {
    }

    public Campaign(String campaignId, String campaignName, String location, String campaignDate, int goal) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.location = location;
        this.campaignDate = campaignDate; // Now accepts a String
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

    public String getCampaignDate() {
        return campaignDate;
    }

    public void setCampaignDate(String campaignDate) {
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return campaignName + " at " + location + " on " + campaignDate;
    }
}