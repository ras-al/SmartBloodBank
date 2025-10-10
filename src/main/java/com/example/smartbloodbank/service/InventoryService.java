package com.example.smartbloodbank.service;

public class InventoryService {
    public int checkInventory(String bloodType) {
        System.out.println("Checking inventory for blood type: " + bloodType);
        return 10;
    }

    public String predictShortages(String hospitalId) {
        System.out.println("Analyzing data to predict shortages for hospital: " + hospitalId);
        return "No shortages predicted for the next 7 days.";
    }
}
