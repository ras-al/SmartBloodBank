package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static final List<User> users = new ArrayList<>();

    // Static block to initialize with some dummy data
    static {
        users.add(new User(1, "donor", "pass", "donor@mail.com", "Donor"));
        users.add(new User(2, "hospital", "pass", "hospital@mail.com", "HospitalStaff"));
        users.add(new User(3, "organizer", "pass", "organizer@mail.com", "CampaignOrganizer"));
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user; // Login successful
            }
        }
        return null; // Login failed
    }
}