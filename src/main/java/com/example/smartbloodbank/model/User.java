package com.example.smartbloodbank.model;

import com.google.cloud.firestore.annotation.Exclude;

public class User {
    private String uid;
    private String username;
    private String password;
    private String email;
    private String role;

    public User() {
    }

    public User(String uid, String username, String password, String email, String role) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    @Exclude
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
    this.role = role;
    }
}