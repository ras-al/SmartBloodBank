package com.example.smartbloodbank.model;

import java.util.ArrayList;
import java.util.List;

public class BloodRequest {
    private int requestId;
    private String bloodType;
    private int unitsRequired;
    private int unitsFulfilled;
    private String status;
    private String postedDate;
    private List<String> acceptedDonors;

    public BloodRequest() {
        this.acceptedDonors = new ArrayList<>();
    }

    public BloodRequest(int requestId, String bloodType, int unitsRequired, String status, String postedDate) {
        this.requestId = requestId;
        this.bloodType = bloodType;
        this.unitsRequired = unitsRequired;
        this.unitsFulfilled = 0;
        this.status = status;
        this.postedDate = postedDate;
        this.acceptedDonors = new ArrayList<>();
    }

    public int getRequestId() {
        return requestId;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    public String getBloodType() {
        return bloodType;
    }
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    public int getUnitsRequired() {
        return unitsRequired;
    }
    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }
    public int getUnitsFulfilled() {
        return unitsFulfilled;
    }
    public void setUnitsFulfilled(int unitsFulfilled) {
        this.unitsFulfilled = unitsFulfilled;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getPostedDate() {
        return postedDate;
    }
    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public List<String> getAcceptedDonors() {
        return acceptedDonors;
    }
    public void setAcceptedDonors(List<String> acceptedDonors) {
        this.acceptedDonors = acceptedDonors;
    }
}