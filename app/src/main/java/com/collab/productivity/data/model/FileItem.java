package com.collab.productivity.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "files")
public class FileItem {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "parent_folder_id")
    private Long parentFolderId; // null if in root

    @ColumnInfo(name = "is_folder")
    private boolean isFolder;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "modified_at")
    private Date modifiedAt;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    @ColumnInfo(name = "size")
    private long size;

    @ColumnInfo(name = "cloudinary_url")
    private String cloudinaryUrl;

    @ColumnInfo(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "parent_path")
    private String parentPath;

    @ColumnInfo(name = "group_id")
    private String groupId; // For collaborative group files

    // Constructor
    public FileItem(String name, String path, String description, Long parentFolderId, boolean isFolder) {
        this.name = name;
        this.path = path;
        this.description = description;
        this.parentFolderId = parentFolderId;
        this.isFolder = isFolder;
        this.createdAt = new Date();
        this.modifiedAt = new Date();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.modifiedAt = new Date();
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.modifiedAt = new Date();
    }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
        this.modifiedAt = new Date();
    }

    public String getCloudinaryUrl() { return cloudinaryUrl; }
    public void setCloudinaryUrl(String cloudinaryUrl) { this.cloudinaryUrl = cloudinaryUrl; }

    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getParentPath() { return parentPath; }
    public void setParentPath(String parentPath) { this.parentPath = parentPath; }

    public boolean isFolder() { return isFolder; }
    public void setFolder(boolean folder) { isFolder = folder; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Date modifiedAt) { this.modifiedAt = modifiedAt; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
