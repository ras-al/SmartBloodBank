package com.example.smartbloodbank.model;

import java.util.HashMap;
import java.util.Map;

public class PartnerHospital {
    private String partnerHospitalId;
    private String hospitalName;
    private String address;
    private String contactNumber;
    private String apiEndPoint;
    private Map<String, Integer> bloodStock;

    public PartnerHospital() {
        this.bloodStock = new HashMap<>();
    }

    public PartnerHospital(String partnerHospitalId, String hospitalName, String address, String contactNumber, String apiEndPoint) {
        this.partnerHospitalId = partnerHospitalId;
        this.hospitalName = hospitalName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.apiEndPoint = apiEndPoint;
        this.bloodStock = new HashMap<>();
    }

    public String getPartnerHospitalId() {
        return partnerHospitalId;
    }
    public void setPartnerHospitalId(String partnerHospitalId) {
        this.partnerHospitalId = partnerHospitalId;
    }
    public String getHospitalName() {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getContactNumber() {
        return contactNumber;
    }
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Getter and Setter updated to match Firestore
    public String getApiEndPoint() {
        return apiEndPoint;
    }
    public void setApiEndPoint(String apiEndPoint) {
        this.apiEndPoint = apiEndPoint;
    }

    public Map<String, Integer> getBloodStock() {
        return bloodStock;
    }
    public void setBloodStock(Map<String, Integer> bloodStock) {
        this.bloodStock = bloodStock;
    }
}