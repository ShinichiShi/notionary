package com.collab.productivity.utils;

import android.util.Log;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.data.model.Note;
import com.collab.productivity.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseManager - Manages Firebase Authentication and Firestore operations
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_FILES = "files";
    private static final String COLLECTION_NOTES = "notes";
    private static final String COLLECTION_GROUPS = "groups";

    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Sign in with email and password
     */
    public void signIn(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Sign in successful");
                    FirebaseUser user = auth.getCurrentUser();
                    updateUserLastLogin(user.getUid());
                    callback.onSuccess(user);
                } else {
                    Log.e(TAG, "Sign in failed", task.getException());
                    String errorMessage = "Sign in failed";
                    if (task.getException() != null) {
                        String exceptionMessage = task.getException().getMessage();
                        if (exceptionMessage != null) {
                            if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Firebase configuration error. Please check your google-services.json file and Firebase Console settings.";
                            } else if (exceptionMessage.contains("network")) {
                                errorMessage = "Network error. Please check your internet connection.";
                            } else if (exceptionMessage.contains("password is invalid") || exceptionMessage.contains("wrong-password")) {
                                errorMessage = "Invalid email or password.";
                            } else if (exceptionMessage.contains("no user record") || exceptionMessage.contains("user-not-found")) {
                                errorMessage = "No account found with this email.";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }
                    }
                    callback.onError(errorMessage);
                }
            });
    }

    /**
     * Sign up with email and password
     */
    public void signUp(String email, String password, String displayName, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Sign up successful");
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Create user document in Firestore
                        User user = new User(firebaseUser.getUid(), email, displayName);
                        createUserDocument(user, callback);
                    }
                } else {
                    Log.e(TAG, "Sign up failed", task.getException());
                    String errorMessage = "Sign up failed";
                    if (task.getException() != null) {
                        String exceptionMessage = task.getException().getMessage();
                        if (exceptionMessage != null) {
                            if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Firebase configuration error. Please check your google-services.json file and Firebase Console settings.";
                            } else if (exceptionMessage.contains("network")) {
                                errorMessage = "Network error. Please check your internet connection.";
                            } else if (exceptionMessage.contains("already in use")) {
                                errorMessage = "This email is already registered.";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }
                    }
                    callback.onError(errorMessage);
                }
            });
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        auth.signOut();
        Log.d(TAG, "User signed out");
    }

    /**
     * Create user document in Firestore
     */
    private void createUserDocument(User user, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("createdAt", user.getCreatedAt());
        userData.put("lastLogin", user.getLastLogin());
        userData.put("storageUsed", user.getStorageUsed());

        db.collection(COLLECTION_USERS)
            .document(user.getUid())
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User document created");
                callback.onSuccess(auth.getCurrentUser());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating user document", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Update user last login time
     */
    private void updateUserLastLogin(String userId) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .update("lastLogin", new java.util.Date())
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Last login updated"))
            .addOnFailureListener(e -> Log.e(TAG, "Error updating last login", e));
    }

    /**
     * Get user data from Firestore
     */
    public void getUserData(String userId, UserCallback callback) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    callback.onSuccess(user);
                } else {
                    callback.onError("User document not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user data", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Save file metadata to Firestore
     */
    public void saveFileToFirestore(FileItem fileItem, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "saveFileToFirestore: User not logged in");
            callback.onError("User not logged in");
            return;
        }

        Log.d(TAG, "saveFileToFirestore: Starting save for file: " + fileItem.getName());
        Log.d(TAG, "saveFileToFirestore: UserId: " + userId);

        fileItem.setUserId(userId);

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("name", fileItem.getName());
        fileData.put("path", fileItem.getPath());
        fileData.put("description", fileItem.getDescription());
        fileData.put("parentFolderId", fileItem.getParentFolderId());
        fileData.put("parentPath", fileItem.getParentPath());
        fileData.put("isFolder", fileItem.isFolder());
        fileData.put("mimeType", fileItem.getMimeType());
        fileData.put("size", fileItem.getSize());
        fileData.put("cloudinaryUrl", fileItem.getCloudinaryUrl());
        fileData.put("cloudinaryPublicId", fileItem.getCloudinaryPublicId());
        fileData.put("userId", userId);
        fileData.put("createdAt", fileItem.getCreatedAt());
        fileData.put("modifiedAt", fileItem.getModifiedAt());

        Log.d(TAG, "saveFileToFirestore: File data prepared, saving to Firestore...");
        Log.d(TAG, "saveFileToFirestore: CloudinaryUrl: " + fileItem.getCloudinaryUrl());
        Log.d(TAG, "saveFileToFirestore: CloudinaryPublicId: " + fileItem.getCloudinaryPublicId());

        // First, ensure the user document exists (required for subcollections)
        ensureUserDocumentExists(userId, new FirestoreCallback() {
            @Override
            public void onSuccess(String documentId) {
                // User document exists, now save the file
                Log.d(TAG, "saveFileToFirestore: User document verified, adding file to subcollection");

                db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_FILES)
                    .add(fileData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "saveFileToFirestore: SUCCESS - File saved to Firestore with ID: " + documentReference.getId());
                        Log.d(TAG, "saveFileToFirestore: Document path: users/" + userId + "/files/" + documentReference.getId());
                        fileItem.setFirestoreId(documentReference.getId());
                        callback.onSuccess(documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveFileToFirestore: FAILED - Error saving file to Firestore", e);
                        Log.e(TAG, "saveFileToFirestore: Error message: " + e.getMessage());
                        Log.e(TAG, "saveFileToFirestore: Error class: " + e.getClass().getName());
                        callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
                    });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "saveFileToFirestore: Failed to verify user document: " + error);
                callback.onError("Failed to verify user document: " + error);
            }
        });
    }

    /**
     * Ensure user document exists in Firestore
     * This is required before creating subcollections
     */
    private void ensureUserDocumentExists(String userId, FirestoreCallback callback) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "ensureUserDocumentExists: User document exists");
                    callback.onSuccess(userId);
                } else {
                    Log.d(TAG, "ensureUserDocumentExists: User document doesn't exist, creating it...");
                    // Create minimal user document
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", userId);
                    userData.put("createdAt", new java.util.Date());
                    userData.put("lastLogin", new java.util.Date());

                    db.collection(COLLECTION_USERS)
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "ensureUserDocumentExists: User document created successfully");
                            callback.onSuccess(userId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "ensureUserDocumentExists: Failed to create user document", e);
                            callback.onError(e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "ensureUserDocumentExists: Failed to check user document", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Update file metadata in Firestore
     */
    public void updateFileInFirestore(FileItem fileItem, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || fileItem.getFirestoreId() == null) {
            callback.onError("User not logged in or file not in Firestore");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", fileItem.getName());
        updates.put("description", fileItem.getDescription());
        updates.put("modifiedAt", fileItem.getModifiedAt());
        updates.put("parentPath", fileItem.getParentPath());

        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FILES)
            .document(fileItem.getFirestoreId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "File updated in Firestore");
                callback.onSuccess(fileItem.getFirestoreId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating file in Firestore", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Delete file from Firestore
     */
    public void deleteFileFromFirestore(String firestoreId, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FILES)
            .document(firestoreId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "File deleted from Firestore");
                callback.onSuccess(firestoreId);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting file from Firestore", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Get all files for current user from Firestore
     */
    public void getUserFiles(FilesCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FILES)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Map<String, Object>> files = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Map<String, Object> fileData = document.getData();
                    fileData.put("firestoreId", document.getId());
                    files.add(fileData);
                }
                callback.onSuccess(files);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user files", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Save group file metadata to Firestore
     */
    public void saveGroupFileToFirestore(FileItem fileItem, String groupId, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "saveGroupFileToFirestore: User not logged in");
            callback.onError("User not logged in");
            return;
        }

        Log.d(TAG, "saveGroupFileToFirestore: Starting save for file: " + fileItem.getName() + " in group: " + groupId);

        fileItem.setUserId(userId);
        fileItem.setGroupId(groupId);

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("name", fileItem.getName());
        fileData.put("path", fileItem.getPath());
        fileData.put("description", fileItem.getDescription());
        fileData.put("parentFolderId", fileItem.getParentFolderId());
        fileData.put("parentPath", fileItem.getParentPath());
        fileData.put("isFolder", fileItem.isFolder());
        fileData.put("mimeType", fileItem.getMimeType());
        fileData.put("size", fileItem.getSize());
        fileData.put("cloudinaryUrl", fileItem.getCloudinaryUrl());
        fileData.put("cloudinaryPublicId", fileItem.getCloudinaryPublicId());
        fileData.put("userId", userId);
        fileData.put("groupId", groupId);
        fileData.put("createdAt", fileItem.getCreatedAt());
        fileData.put("modifiedAt", fileItem.getModifiedAt());

        Log.d(TAG, "saveGroupFileToFirestore: Saving to groups/" + groupId + "/files");

        db.collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(COLLECTION_FILES)
            .add(fileData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "saveGroupFileToFirestore: SUCCESS - File saved with ID: " + documentReference.getId());
                fileItem.setFirestoreId(documentReference.getId());
                callback.onSuccess(documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "saveGroupFileToFirestore: FAILED - Error: " + e.getMessage(), e);
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            });
    }

    /**
     * Get all files for a group from Firestore
     */
    public void getGroupFiles(String groupId, FilesCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        Log.d(TAG, "getGroupFiles: Loading files for group: " + groupId);

        db.collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(COLLECTION_FILES)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Map<String, Object>> files = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Map<String, Object> fileData = document.getData();
                    fileData.put("firestoreId", document.getId());
                    files.add(fileData);
                }
                Log.d(TAG, "getGroupFiles: Loaded " + files.size() + " files");
                callback.onSuccess(files);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting group files", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Save note to Firestore
     */
    public void saveNoteToFirestore(Note note, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        note.setUserId(userId);

        Map<String, Object> noteData = new HashMap<>();
        noteData.put("title", note.getTitle());
        noteData.put("content", note.getContent());
        noteData.put("color", note.getColor());
        noteData.put("userId", userId);
        noteData.put("createdAt", note.getCreatedAt());
        noteData.put("modifiedAt", note.getModifiedAt());

        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_NOTES)
            .add(noteData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Note saved to Firestore: " + documentReference.getId());
                note.setFirestoreId(documentReference.getId());
                callback.onSuccess(documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving note to Firestore", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Update note in Firestore
     */
    public void updateNoteInFirestore(Note note, FirestoreCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || note.getFirestoreId() == null) {
            callback.onError("User not logged in or note not in Firestore");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", note.getTitle());
        updates.put("content", note.getContent());
        updates.put("color", note.getColor());
        updates.put("modifiedAt", note.getModifiedAt());

        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_NOTES)
            .document(note.getFirestoreId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Note updated in Firestore");
                callback.onSuccess(note.getFirestoreId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating note in Firestore", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Callback interfaces
     */
    /**
     * Create a group in Firestore
     */
    public void createGroup(com.collab.productivity.data.model.Group group, GroupCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        group.setCreatorId(userId);
        group.addMemberId(userId);

        // Generate invite code
        String inviteCode = generateInviteCode();
        group.setInviteCode(inviteCode);

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("id", group.getId());
        groupData.put("name", group.getName());
        groupData.put("description", group.getDescription());
        groupData.put("createdDate", group.getCreatedDate());
        groupData.put("creatorId", userId);
        groupData.put("memberIds", group.getMemberIds());
        groupData.put("members", group.getMembers());
        groupData.put("itemCount", group.getItemCount());
        groupData.put("inviteCode", inviteCode);
        groupData.put("createdAt", new java.util.Date());

        db.collection("groups")
            .document(group.getId())
            .set(groupData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Group created successfully: " + group.getId());
                callback.onSuccess(group);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating group", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Join a group using invite code
     */
    public void joinGroup(String inviteCode, GroupCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection("groups")
            .whereEqualTo("inviteCode", inviteCode)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onError("Invalid invite code");
                    return;
                }

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    com.collab.productivity.data.model.Group group = document.toObject(com.collab.productivity.data.model.Group.class);

                    // Check if user is already a member
                    if (group.getMemberIds() != null && group.getMemberIds().contains(userId)) {
                        callback.onError("You are already a member of this group");
                        return;
                    }

                    // Add user to group
                    List<String> updatedMemberIds = group.getMemberIds();
                    if (updatedMemberIds == null) {
                        updatedMemberIds = new ArrayList<>();
                    }
                    updatedMemberIds.add(userId);

                    final List<String> finalMemberIds = updatedMemberIds;

                    db.collection("groups")
                        .document(group.getId())
                        .update("memberIds", finalMemberIds)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User joined group successfully");
                            group.setMemberIds(finalMemberIds);
                            callback.onSuccess(group);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error joining group", e);
                            callback.onError(e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error finding group", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Get groups for current user
     */
    public void getUserGroups(GroupsCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<com.collab.productivity.data.model.Group> groups = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    com.collab.productivity.data.model.Group group = document.toObject(com.collab.productivity.data.model.Group.class);
                    groups.add(group);
                }
                Log.d(TAG, "Retrieved " + groups.size() + " groups");
                callback.onSuccess(groups);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user groups", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Get group by ID
     */
    public void getGroupById(String groupId, GroupCallback callback) {
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    com.collab.productivity.data.model.Group group = documentSnapshot.toObject(com.collab.productivity.data.model.Group.class);
                    callback.onSuccess(group);
                } else {
                    callback.onError("Group not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting group", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Generate a random invite code
     */
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // Callback Interfaces
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface FirestoreCallback {
        void onSuccess(String documentId);
        void onError(String error);
    }

    public interface FilesCallback {
        void onSuccess(List<Map<String, Object>> files);
        void onError(String error);
    }

    public interface GroupCallback {
        void onSuccess(com.collab.productivity.data.model.Group group);
        void onError(String error);
    }

    public interface GroupsCallback {
        void onSuccess(List<com.collab.productivity.data.model.Group> groups);
        void onError(String error);
    }
}

