package com.example.smartbloodbank.model;

public class HospitalStaff extends User {
    private String hospitalId;
    private String hospitalName;

    public HospitalStaff(int userId, String username, String password, String email, String hospitalId, String hospitalName) {
        super(userId, username, password, email, "HospitalStaff");
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
    }

    public String getHospitalId() {
        return hospitalId;
    }
    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }
    public String getHospitalName() {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}