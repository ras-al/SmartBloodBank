package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.User;
import java.util.ArrayList;
import java.util.List;
import com.example.smartbloodbank.model.Donor;
import com.example.smartbloodbank.model.HospitalStaff;
import com.example.smartbloodbank.model.CampaignOrganizer;

public class AuthService {
    private static final List<User> users = new ArrayList<>();

    // Static block to initialize with some dummy data
    static {
        users.add(new Donor(1, "donor", "pass", "donor@mail.com", "B+", "Kollam"));
        users.add(new HospitalStaff(2, "hospital", "pass", "hospital@mail.com", "HSP101", "Mercy Hospital"));
        users.add(new CampaignOrganizer(3, "organizer", "pass", "organizer@mail.com", "ORG789", "Red Cross"));
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}