package com.example.smartbloodbank.model;

import java.util.HashMap;
import java.util.Map;

public class PartnerHospital {
    private String hospitalName;
    private String address;
    private String contactNumber;
    private Map<String, Integer> bloodStock;

    public PartnerHospital(String hospitalName, String address, String contactNumber) {
        this.hospitalName = hospitalName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.bloodStock = new HashMap<>(); // Initialize the map
    }

    // --- Getters and Setters ---
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Map<String, Integer> getBloodStock() { return bloodStock; }

    // Helper method to easily add stock
    public void addStock(String bloodType, int units) {
        this.bloodStock.put(bloodType, units);
    }
}