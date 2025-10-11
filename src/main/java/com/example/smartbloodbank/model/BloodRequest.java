package com.example.smartbloodbank.model;

import java.time.LocalDate;

public class BloodRequest {
    private int requestId;
    private String bloodType;
    private int unitsRequired;
    private String status;
    private String postedDate;

    public BloodRequest() {

    }

    public BloodRequest(int requestId, String bloodType, int unitsRequired, String status, String postedDate) {
        this.requestId = requestId;
        this.bloodType = bloodType;
        this.unitsRequired = unitsRequired;
        this.status = status;
        this.postedDate = postedDate;
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
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}