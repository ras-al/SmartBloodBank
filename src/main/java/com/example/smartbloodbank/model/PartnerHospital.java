package com.example.smartbloodbank.model;

public class PartnerHospital {
    private String hospitalName;
    private String address;
    private String contactNumber;

    public PartnerHospital(String hospitalName, String address, String contactNumber) {
        this.hospitalName = hospitalName;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
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

    // This method controls how the object is displayed in a ListView
    @Override
    public String toString() {
        return hospitalName + " - " + address;
    }
}