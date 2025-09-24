package com.example.smartbloodbank.model;

public class CampaignOrganizer extends User {
    private String organization; // e.g., "Red Cross", "Lions Club"

    public CampaignOrganizer(int userId, String username, String password, String email, String organization) {
        super(userId, username, password, email, "CampaignOrganizer");
        this.organization = organization;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}