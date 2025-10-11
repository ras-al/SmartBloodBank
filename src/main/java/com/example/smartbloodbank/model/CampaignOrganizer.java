package com.example.smartbloodbank.model;

import java.util.ArrayList;
import java.util.List;

public class CampaignOrganizer extends User {
    private String organizationId;
    private String organizationName;
    private List<Campaign> createdCampaigns;

    public CampaignOrganizer() {
        super();
    }
    public CampaignOrganizer(String userId, String username, String password, String email, String organizationId, String organizationName) {
        super(userId, username, password, email, "CampaignOrganizer");
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.createdCampaigns = new ArrayList<>();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public List<Campaign> getCreatedCampaigns() {
        return createdCampaigns;
    }

    public void addCampaign(Campaign campaign) {
        this.createdCampaigns.add(campaign);
    }
}