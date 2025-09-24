package com.example.smartbloodbank.service;

import com.example.smartbloodbank.model.User;
import com.example.smartbloodbank.model.BloodRequest;

// contain the logic for matching donors to blood requests.
public class MatchingService {

    public User findBloodSource(BloodRequest request) {
        // Dummy Logic: In the future, this will search the database(now its just print msg and return nothing)
        System.out.println("Searching for a match for " + request.getUnitsRequired() + " units of " + request.getBloodType());
        return null; // Returning null because we don't have real donar.
    }
}