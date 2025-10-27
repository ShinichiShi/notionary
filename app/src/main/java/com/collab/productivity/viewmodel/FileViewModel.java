package com.collab.productivity.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.data.repository.FileRepository;
import com.collab.productivity.utils.CloudinaryManager;
import com.collab.productivity.utils.FirebaseManager;
import com.collab.productivity.utils.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileViewModel extends AndroidViewModel {
    private static final String TAG = "FileViewModel";
    private final FileRepository repository;
    private final MutableLiveData<Long> currentFolderId;
    private final MutableLiveData<String> currentPath;
    private final MutableLiveData<String> statusMessage;
    private final MediatorLiveData<List<FileItem>> currentFiles;
    private final MutableLiveData<Integer> uploadProgress;
    private final CloudinaryManager cloudinaryManager;
    private final FirebaseManager firebaseManager;
    private final Stack<NavigationState> navigationStack;
    private LiveData<List<FileItem>> currentSource;

    private static class NavigationState {
        Long folderId;
        String path;

        NavigationState(Long folderId, String path) {
            this.folderId = folderId;
            this.path = path;
        }
    }

    public FileViewModel(Application application) {
        super(application);
        repository = new FileRepository(application);
        currentFolderId = new MutableLiveData<>(null);
        currentPath = new MutableLiveData<>("/");
        statusMessage = new MutableLiveData<>();
        currentFiles = new MediatorLiveData<>();
        uploadProgress = new MutableLiveData<>(0);
        cloudinaryManager = CloudinaryManager.getInstance();
        firebaseManager = FirebaseManager.getInstance();
        navigationStack = new Stack<>();

        Logger.d(TAG, "FileViewModel initialized");

        // Set up observer for folder changes to automatically load appropriate files
        currentFolderId.observeForever(folderId -> {
            Logger.d(TAG, "Current folder ID changed to: " + folderId + ", loading files...");
            loadFilesForCurrentFolder();
        });

        // Initialize with root items
        loadFilesForCurrentFolder();
    }

    /**
     * Loads files for the current folder using MediatorLiveData
     * This automatically switches data sources when navigating between folders
     */
    private void loadFilesForCurrentFolder() {
        // Remove previous source if exists
        if (currentSource != null) {
            currentFiles.removeSource(currentSource);
        }

        Long folderId = currentFolderId.getValue();

        if (folderId == null) {
            // Load root items
            Logger.d(TAG, "Loading root items from database");
            currentSource = repository.getRootItems();
        } else {
            // Load folder items
            Logger.d(TAG, "Loading items in folder: " + folderId);
            currentSource = repository.getItemsInFolder(folderId);
        }

        // Add new source to MediatorLiveData
        currentFiles.addSource(currentSource, items -> {
            if (items != null) {
                Logger.d(TAG, "Files loaded: " + items.size() + " items");
                currentFiles.setValue(items);
            }
        });
    }

    /**
     * Manually update file list (optional - MediatorLiveData handles this automatically)
     * Kept for backward compatibility
     */
    public void updateFileList(List<FileItem> files) {
        Logger.d(TAG, "Manually updating file list with " + files.size() + " items");
        // Note: This is usually not needed as MediatorLiveData updates automatically
        currentFiles.setValue(files);
    }

    public LiveData<List<FileItem>> getCurrentFolderContents() {
        return currentFiles;
    }

    public LiveData<List<FileItem>> getAllFiles() {
        Logger.d(TAG, "Getting all files from database for debugging");
        return repository.getAllFiles();
    }

    public void createFolder(String name, String description) {
        Logger.d(TAG, "Creating folder: " + name);
        String userId = firebaseManager.getCurrentUserId();

        if (userId == null) {
            Logger.e(TAG, "User not logged in", null);
            statusMessage.setValue("Error: User not logged in");
            return;
        }

        String parentPath = currentPath.getValue();
        if (parentPath == null) {
            parentPath = "/";
        }
        String fullPath = parentPath.equals("/") ? "/" + name : parentPath + "/" + name;

        FileItem folder = new FileItem(name, fullPath, description, currentFolderId.getValue(), true);
        folder.setParentPath(parentPath);
        folder.setUserId(userId);

        // Save to Firestore first
        firebaseManager.saveFileToFirestore(folder, new FirebaseManager.FirestoreCallback() {
            @Override
            public void onSuccess(String documentId) {
                Logger.d(TAG, "Folder synced to Firestore: " + documentId);
                folder.setFirestoreId(documentId);

                // Insert to local database
                repository.insert(folder, new FileRepository.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess(long id) {
                        Logger.d(TAG, "Folder created locally with id: " + id);
                        folder.setId(id);

                        // Update with Firestore ID
                        repository.update(folder, new FileRepository.OnOperationCompleteListener() {
                            @Override
                            public void onSuccess(long updateId) {
                                Logger.d(TAG, "Folder updated with Firestore ID");
                                statusMessage.setValue("Folder created successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Logger.e(TAG, "Error updating folder with Firestore ID", e);
                                statusMessage.setValue("Folder created successfully");
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Logger.e(TAG, "Error creating folder locally", e);
                        statusMessage.setValue("Folder synced to cloud but local save failed");
                    }
                });
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "Error syncing folder to Firestore: " + error, null);
                statusMessage.setValue("Error syncing folder to cloud: " + error);

                // Still save locally even if Firestore fails
                repository.insert(folder, new FileRepository.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess(long id) {
                        Logger.d(TAG, "Folder saved locally despite Firestore error, id: " + id);
                        statusMessage.setValue("Folder created locally (sync pending)");
                    }

                    @Override
                    public void onError(Exception e) {
                        Logger.e(TAG, "Error creating folder locally after Firestore failure", e);
                        statusMessage.setValue("Error creating folder");
                    }
                });
            }
        });
    }

    public void uploadFile(String name, String path, String description, long size, String mimeType) {
        Logger.d(TAG, "Uploading file: " + name);
        String userId = firebaseManager.getCurrentUserId();

        if (userId == null) {
            Logger.e(TAG, "User not logged in", null);
            statusMessage.setValue("Error: User not logged in");
            return;
        }

        String parentPath = currentPath.getValue();
        if (parentPath == null) {
            parentPath = "/";
        }
        String fullPath = parentPath.equals("/") ? "/" + name : parentPath + "/" + name;

        FileItem file = new FileItem(name, fullPath, description, currentFolderId.getValue(), false);
        file.setSize(size);
        file.setMimeType(mimeType);
        file.setParentPath(parentPath);
        file.setUserId(userId);
        file.setPath(path); // Set the actual file path

        statusMessage.setValue("Uploading to cloud...");
        uploadProgress.setValue(0);

        // Upload to Cloudinary first
        Uri fileUri = Uri.parse("file://" + path);
        cloudinaryManager.uploadFile(fileUri, userId, parentPath,
            new CloudinaryManager.CloudinaryUploadCallback() {
                @Override
                public void onProgress(int progress) {
                    Logger.d(TAG, "Upload progress: " + progress + "%");
                    uploadProgress.setValue(progress);
                }

                @Override
                public void onSuccess(String secureUrl, String publicId) {
                    Logger.d(TAG, "File uploaded to Cloudinary successfully");
                    Logger.d(TAG, "Secure URL: " + secureUrl);
                    Logger.d(TAG, "Public ID: " + publicId);

                    file.setCloudinaryUrl(secureUrl);
                    file.setCloudinaryPublicId(publicId);

                    // Save to Firestore first (before local DB to ensure sync)
                    firebaseManager.saveFileToFirestore(file,
                        new FirebaseManager.FirestoreCallback() {
                            @Override
                            public void onSuccess(String documentId) {
                                Logger.d(TAG, "File metadata synced to Firestore: " + documentId);
                                file.setFirestoreId(documentId);

                                // Now insert to local database
                                repository.insert(file, new FileRepository.OnOperationCompleteListener() {
                                    @Override
                                    public void onSuccess(long id) {
                                        Logger.d(TAG, "File saved locally with id: " + id);
                                        file.setId(id);

                                        // Update with Firestore ID
                                        repository.update(file, new FileRepository.OnOperationCompleteListener() {
                                            @Override
                                            public void onSuccess(long updateId) {
                                                Logger.d(TAG, "File updated with Firestore ID");
                                                statusMessage.setValue("File uploaded successfully");
                                                uploadProgress.setValue(100);

                                                // Notify UI to refresh
                                                new android.os.Handler(android.os.Looper.getMainLooper())
                                                    .postDelayed(() -> uploadProgress.setValue(0), 500);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Logger.e(TAG, "Error updating file with Firestore ID", e);
                                                statusMessage.setValue("File uploaded successfully");
                                                uploadProgress.setValue(0);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Logger.e(TAG, "Error saving file locally", e);
                                        statusMessage.setValue("File uploaded to cloud but local save failed");
                                        uploadProgress.setValue(0);
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                Logger.e(TAG, "Error syncing to Firestore: " + error, null);
                                statusMessage.setValue("File uploaded to cloud but Firestore sync failed: " + error);
                                uploadProgress.setValue(0);

                                // Still save locally even if Firestore fails
                                repository.insert(file, new FileRepository.OnOperationCompleteListener() {
                                    @Override
                                    public void onSuccess(long id) {
                                        Logger.d(TAG, "File saved locally despite Firestore error, id: " + id);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Logger.e(TAG, "Error saving file locally after Firestore failure", e);
                                    }
                                });
                            }
                        });
                }

                @Override
                public void onError(String error) {
                    Logger.e(TAG, "Error uploading to Cloudinary: " + error, null);
                    statusMessage.setValue("Error uploading file: " + error);
                    uploadProgress.setValue(0);
                }
            });
    }

    public void navigateToFolder(Long folderId, String folderPath) {
        Logger.d(TAG, "Navigating to folder: " + folderPath);
        // Save current state to navigation stack
        navigationStack.push(new NavigationState(currentFolderId.getValue(), currentPath.getValue()));

        currentFolderId.setValue(folderId);
        currentPath.setValue(folderPath);
    }

    public void navigateToParent() {
        Logger.d(TAG, "Navigating to parent folder");
        if (!navigationStack.isEmpty()) {
            NavigationState prevState = navigationStack.pop();
            currentFolderId.setValue(prevState.folderId);
            currentPath.setValue(prevState.path);
        } else {
            // If no navigation history, go to root
            Logger.d(TAG, "No navigation history, going to root");
            currentFolderId.setValue(null);
            currentPath.setValue("/");
        }
    }

    public void updateItem(FileItem item) {
        Logger.d(TAG, "Updating item: " + item.getName());
        repository.update(item, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "Item updated successfully");

                // Update in Firestore
                if (item.getFirestoreId() != null) {
                    firebaseManager.updateFileInFirestore(item,
                        new FirebaseManager.FirestoreCallback() {
                            @Override
                            public void onSuccess(String documentId) {
                                Logger.d(TAG, "Item synced to Firestore");
                            }

                            @Override
                            public void onError(String error) {
                                Logger.e(TAG, "Error syncing to Firestore: " + error, null);
                            }
                        });
                }

                statusMessage.setValue("Item updated successfully");
            }

            @Override
            public void onError(Exception e) {
                Logger.e(TAG, "Error updating item", e);
                statusMessage.setValue("Error updating item");
            }
        });
    }

    public void deleteItem(FileItem item) {
        Logger.d(TAG, "Deleting item: " + item.getName());

        // Delete from Cloudinary if it's a file with a cloudinary URL
        if (!item.isFolder() && item.getCloudinaryPublicId() != null) {
            cloudinaryManager.deleteFile(item.getCloudinaryPublicId(),
                new CloudinaryManager.CloudinaryDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        Logger.d(TAG, "File deleted from Cloudinary");
                    }

                    @Override
                    public void onError(String error) {
                        Logger.e(TAG, "Error deleting from Cloudinary: " + error, null);
                    }
                });
        }

        repository.delete(item, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "Item deleted successfully");

                // Delete from Firestore
                if (item.getFirestoreId() != null) {
                    firebaseManager.deleteFileFromFirestore(item.getFirestoreId(),
                        new FirebaseManager.FirestoreCallback() {
                            @Override
                            public void onSuccess(String documentId) {
                                Logger.d(TAG, "Item deleted from Firestore");
                            }

                            @Override
                            public void onError(String error) {
                                Logger.e(TAG, "Error deleting from Firestore: " + error, null);
                            }
                        });
                }

                statusMessage.setValue("Item deleted successfully");
            }

            @Override
            public void onError(Exception e) {
                Logger.e(TAG, "Error deleting item", e);
                statusMessage.setValue("Error deleting item");
            }
        });
    }

    public LiveData<String> getCurrentPath() {
        return currentPath;
    }

    public LiveData<Long> getCurrentFolderId() {
        return currentFolderId;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Integer> getUploadProgress() {
        return uploadProgress;
    }

    public LiveData<List<FileItem>> getRootItems() {
        return repository.getRootItems();
    }

    public LiveData<List<FileItem>> getItemsInFolder(Long folderId) {
        return repository.getItemsInFolder(folderId);
    }

    public void updatePath(String newPath) {
        Logger.d(TAG, "Updating path to: " + newPath);
        currentPath.setValue(newPath);
    }

    /**
     * Sync files from Firestore to local database
     */
    public void syncFilesFromFirestore(SyncCallback callback) {
        Logger.d(TAG, "Syncing files from Firestore");
        String userId = firebaseManager.getCurrentUserId();

        if (userId == null) {
            Logger.e(TAG, "User not logged in", null);
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        firebaseManager.getUserFiles(new FirebaseManager.FilesCallback() {
            @Override
            public void onSuccess(List<java.util.Map<String, Object>> files) {
                Logger.d(TAG, "Received " + files.size() + " files from Firestore");

                // Process each file and update local database
                for (java.util.Map<String, Object> fileData : files) {
                    try {
                        FileItem fileItem = convertMapToFileItem(fileData);

                        // Check if file already exists locally by firestoreId
                        repository.getItemByFirestoreId(fileItem.getFirestoreId(), new FileRepository.OnItemRetrievedListener() {
                            @Override
                            public void onSuccess(FileItem existingItem) {
                                if (existingItem != null) {
                                    // Update existing item
                                    fileItem.setId(existingItem.getId());
                                    repository.update(fileItem, null);
                                } else {
                                    // Insert new item
                                    repository.insert(fileItem, null);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                Logger.e(TAG, "Error checking existing item", e);
                            }
                        });
                    } catch (Exception e) {
                        Logger.e(TAG, "Error processing file from Firestore", e);
                    }
                }

                statusMessage.setValue("Synced " + files.size() + " files from cloud");
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "Error syncing files: " + error, null);
                statusMessage.setValue("Error syncing files: " + error);
                if (callback != null) callback.onError(error);
            }
        });
    }

    /**
     * Convert Firestore map data to FileItem
     * Handles Firestore Timestamp objects properly
     */
    private FileItem convertMapToFileItem(java.util.Map<String, Object> data) {
        String name = (String) data.get("name");
        String path = (String) data.get("path");
        String description = (String) data.get("description");
        Boolean isFolder = (Boolean) data.get("isFolder");

        FileItem item = new FileItem(
            name != null ? name : "",
            path != null ? path : "",
            description != null ? description : "",
            null, // parentFolderId will be set if needed
            isFolder != null ? isFolder : false
        );

        item.setFirestoreId((String) data.get("firestoreId"));
        item.setUserId((String) data.get("userId"));
        item.setParentPath((String) data.get("parentPath"));
        item.setCloudinaryUrl((String) data.get("cloudinaryUrl"));
        item.setCloudinaryPublicId((String) data.get("cloudinaryPublicId"));
        item.setMimeType((String) data.get("mimeType"));

        if (data.get("size") != null) {
            item.setSize(((Number) data.get("size")).longValue());
        }

        // Handle Firestore Timestamp conversion for createdAt
        if (data.get("createdAt") != null) {
            Object createdAt = data.get("createdAt");
            if (createdAt instanceof com.google.firebase.Timestamp) {
                item.setCreatedAt(((com.google.firebase.Timestamp) createdAt).toDate());
            } else if (createdAt instanceof java.util.Date) {
                item.setCreatedAt((java.util.Date) createdAt);
            } else {
                Logger.w(TAG, "Unknown createdAt type: " + createdAt.getClass().getName());
            }
        }

        // Handle Firestore Timestamp conversion for modifiedAt
        if (data.get("modifiedAt") != null) {
            Object modifiedAt = data.get("modifiedAt");
            if (modifiedAt instanceof com.google.firebase.Timestamp) {
                item.setModifiedAt(((com.google.firebase.Timestamp) modifiedAt).toDate());
            } else if (modifiedAt instanceof java.util.Date) {
                item.setModifiedAt((java.util.Date) modifiedAt);
            } else {
                Logger.w(TAG, "Unknown modifiedAt type: " + modifiedAt.getClass().getName());
            }
        }

        return item;
    }

    /**
     * Reload files from local database for current folder
     */
    public void reloadCurrentFolder() {
        Logger.d(TAG, "Reloading current folder");
        Long folderId = currentFolderId.getValue();

        if (folderId == null) {
            repository.getRootItems().observeForever(items -> {
                if (items != null) {
                    Logger.d(TAG, "Reloaded " + items.size() + " root items");
                    currentFiles.setValue(items);
                }
            });
        } else {
            repository.getItemsInFolder(folderId).observeForever(items -> {
                if (items != null) {
                    Logger.d(TAG, "Reloaded " + items.size() + " items in folder " + folderId);
                    currentFiles.setValue(items);
                }
            });
        }
    }

    /**
     * Callback interface for sync operations
     */
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
}

