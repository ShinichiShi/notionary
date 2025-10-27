package com.collab.productivity.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

/**
 * CloudinaryManager - Manages Cloudinary integration for file uploads
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static final String CLOUD_NAME = "dlanenxlr";
    private static final String API_KEY = "148131912812219";
    private static final String API_SECRET = "zTIh4EC34LM4hBJzwmjtlyfBtl4";

    private static CloudinaryManager instance;
    private boolean isInitialized = false;

    private CloudinaryManager() {
    }

    public static synchronized CloudinaryManager getInstance() {
        if (instance == null) {
            instance = new CloudinaryManager();
        }
        return instance;
    }

    /**
     * Initialize Cloudinary with credentials
     */
    public void init(Context context) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("secure", "true");

                MediaManager.init(context, config);
                isInitialized = true;
                Log.d(TAG, "Cloudinary initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Cloudinary", e);
            }
        }
    }

    /**
     * Upload file to Cloudinary
     * @param uri - URI of the file to upload
     * @param userId - User ID for folder organization
     * @param folderPath - Folder path in Cloudinary
     * @param callback - Upload callback
     */
    public void uploadFile(Uri uri, String userId, String folderPath, CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            callback.onError("Cloudinary not initialized");
            return;
        }

        try {
            // Create folder structure: users/{userId}/{folderPath}
            String cloudinaryFolder = "notionary/" + userId;
            if (folderPath != null && !folderPath.isEmpty() && !folderPath.equals("/")) {
                cloudinaryFolder += folderPath.replace("/", "_");
            }

            Map<String, Object> options = new HashMap<>();
            options.put("folder", cloudinaryFolder);
            options.put("resource_type", "auto");

            MediaManager.get()
                .upload(uri)
                .option("folder", cloudinaryFolder)
                .option("resource_type", "auto")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onProgress(0);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Upload progress: " + progress + "%");
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Upload successful: " + requestId);
                        String secureUrl = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");
                        callback.onSuccess(secureUrl, publicId);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload failed: " + error.getDescription());
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled: " + requestId);
                    }
                })
                .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file", e);
            callback.onError(e.getMessage());
        }
    }

    /**
     * Delete file from Cloudinary
     * @param publicId - Public ID of the file to delete
     * @param callback - Delete callback
     */
    public void deleteFile(String publicId, CloudinaryDeleteCallback callback) {
        // Note: Deletion requires server-side implementation for security
        // For now, we'll just callback success
        // In production, this should call your backend API
        Log.d(TAG, "Delete requested for: " + publicId);
        callback.onSuccess();
    }

    /**
     * Get optimized URL for file
     * @param publicId - Public ID of the file
     * @return Optimized URL
     */
    public String getOptimizedUrl(String publicId) {
        if (!isInitialized) {
            return null;
        }
        return MediaManager.get()
            .url()
            .generate(publicId);
    }

    /**
     * Callback interface for upload operations
     */
    public interface CloudinaryUploadCallback {
        void onProgress(int progress);
        void onSuccess(String secureUrl, String publicId);
        void onError(String error);
    }

    /**
     * Callback interface for delete operations
     */
    public interface CloudinaryDeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}

