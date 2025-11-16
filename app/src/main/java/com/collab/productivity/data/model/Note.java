package com.collab.productivity.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "modified_at")
    private Date modifiedAt;

    @ColumnInfo(name = "color")
    private int color; // For note card colors

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "synced")
    private boolean synced;

    @ColumnInfo(name = "mood_emoji")
    private String moodEmoji; // Store emoji character

    @ColumnInfo(name = "mood_type")
    private String moodType; // happy, sad, excited, calm, etc.

    @ColumnInfo(name = "mood_intensity")
    private int moodIntensity; // 1-5 scale

    @ColumnInfo(name = "tags")
    private String tags; // Comma-separated tags

    // Constructor
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = new Date();
        this.modifiedAt = new Date();
        this.color = 0; // Default color
        this.moodEmoji = "😐"; // Default neutral emoji
        this.moodType = "neutral";
        this.moodIntensity = 3;
        this.synced = false;
    }

    // Constructor with mood
    @androidx.room.Ignore
    public Note(String title, String content, String moodEmoji, String moodType, int moodIntensity) {
        this(title, content);
        this.moodEmoji = moodEmoji;
        this.moodType = moodType;
        this.moodIntensity = moodIntensity;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.modifiedAt = new Date();
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.modifiedAt = new Date();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Date modifiedAt) { this.modifiedAt = modifiedAt; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    // Mood-related getters and setters
    public String getMoodEmoji() { return moodEmoji; }
    public void setMoodEmoji(String moodEmoji) {
        this.moodEmoji = moodEmoji;
        this.modifiedAt = new Date();
    }

    public String getMoodType() { return moodType; }
    public void setMoodType(String moodType) {
        this.moodType = moodType;
        this.modifiedAt = new Date();
    }

    public int getMoodIntensity() { return moodIntensity; }
    public void setMoodIntensity(int moodIntensity) {
        this.moodIntensity = Math.max(1, Math.min(5, moodIntensity)); // Clamp between 1-5
        this.modifiedAt = new Date();
    }

    public String getTags() { return tags; }
    public void setTags(String tags) {
        this.tags = tags;
        this.modifiedAt = new Date();
    }

    // Utility methods
    public boolean hasEmptyTitle() {
        return title == null || title.trim().isEmpty();
    }

    public boolean hasEmptyContent() {
        return content == null || content.trim().isEmpty();
    }

    public String getDisplayTitle() {
        if (hasEmptyTitle()) {
            return hasEmptyContent() ? "Untitled Note" :
                    content.length() > 30 ? content.substring(0, 30) + "..." : content;
        }
        return title;
    }

    public String getMoodDisplay() {
        if (moodEmoji != null && !moodEmoji.isEmpty()) {
            return moodEmoji + " " + (moodType != null ? moodType : "");
        }
        return "";
    }
}
