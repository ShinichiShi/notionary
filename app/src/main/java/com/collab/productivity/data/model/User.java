package com.collab.productivity.data.model;

import java.util.Date;

/**
 * User model for Firebase Authentication and Firestore
 */
public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private Date createdAt;
    private Date lastLogin;
    private long storageUsed; // in bytes

    public User() {
        // Default constructor required for Firebase
    }

    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = new Date();
        this.lastLogin = new Date();
        this.storageUsed = 0;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(long storageUsed) { this.storageUsed = storageUsed; }
}

