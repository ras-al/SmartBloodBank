package com.example.smartbloodbank.model;

import java.util.HashMap;
import java.util.Map;

public class PartnerHospital {
    private String partnerHospitalId;
    private String hospitalName;
    private String address;
    private String contactNumber;
    private String apiEndpoint;
    private Map<String, Integer> bloodStock;

    public PartnerHospital(String partnerHospitalId, String hospitalName, String address, String contactNumber, String apiEndpoint) {
        this.partnerHospitalId = partnerHospitalId;
        this.hospitalName = hospitalName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.apiEndpoint = apiEndpoint;
        this.bloodStock = new HashMap<>(); // Initialize the map
    }

    // --- Getters and Setters ---
    public String getPartnerHospitalId() { return partnerHospitalId; }
    public void setPartnerHospitalId(String partnerHospitalId) { this.partnerHospitalId = partnerHospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    public Map<String, Integer> getBloodStock() { return bloodStock; }
    public void addStock(String bloodType, int units) {
        this.bloodStock.put(bloodType, units);
    }
}