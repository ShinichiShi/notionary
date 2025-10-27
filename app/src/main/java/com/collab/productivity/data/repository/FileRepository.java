package com.collab.productivity.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.collab.productivity.NotionaryApp;
import com.collab.productivity.data.dao.FileDao;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.utils.Logger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileRepository {
    private static final String TAG = "FileRepository";
    private final FileDao fileDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public FileRepository(Context context) {
        NotionaryApp app = (NotionaryApp) context.getApplicationContext();
        fileDao = app.getDatabase().fileDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        Logger.d(TAG, "FileRepository initialized");
    }

    public LiveData<List<FileItem>> getRootItems() {
        Logger.d(TAG, "Getting root items");
        return fileDao.getRootItemsSorted();
    }

    public LiveData<List<FileItem>> getAllFiles() {
        Logger.d(TAG, "Getting all files");
        return fileDao.getAllFiles();
    }

    public LiveData<List<FileItem>> getItemsInFolder(Long folderId) {
        Logger.d(TAG, "Getting items in folder: " + folderId);
        return fileDao.getItemsInFolderSorted(folderId);
    }

    public LiveData<FileItem> getItemById(long id) {
        Logger.d(TAG, "Getting item by id: " + id);
        return fileDao.getItemById(id);
    }

    public void insert(FileItem fileItem, OnOperationCompleteListener listener) {
        Logger.d(TAG, "Inserting item: " + fileItem.getName());
        executorService.execute(() -> {
            try {
                long id = fileDao.insert(fileItem);
                fileItem.setId(id);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(id);
                    }
                });
                Logger.d(TAG, "Item inserted with id: " + id);
            } catch (Exception e) {
                Logger.e(TAG, "Error inserting item", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void update(FileItem fileItem, OnOperationCompleteListener listener) {
        Logger.d(TAG, "Updating item: " + fileItem.getName());
        executorService.execute(() -> {
            try {
                fileDao.update(fileItem);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(fileItem.getId());
                    }
                });
                Logger.d(TAG, "Item updated successfully");
            } catch (Exception e) {
                Logger.e(TAG, "Error updating item", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void delete(FileItem fileItem, OnOperationCompleteListener listener) {
        Logger.d(TAG, "Deleting item: " + fileItem.getName());
        executorService.execute(() -> {
            try {
                fileDao.delete(fileItem);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(fileItem.getId());
                    }
                });
                Logger.d(TAG, "Item deleted successfully");
            } catch (Exception e) {
                Logger.e(TAG, "Error deleting item", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void moveItem(long itemId, Long newParentId, OnOperationCompleteListener listener) {
        Logger.d(TAG, "Moving item " + itemId + " to parent " + newParentId);
        executorService.execute(() -> {
            try {
                fileDao.moveItem(itemId, newParentId);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(itemId);
                    }
                });
                Logger.d(TAG, "Item moved successfully");
            } catch (Exception e) {
                Logger.e(TAG, "Error moving item", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public LiveData<List<FileItem>> searchItems(String query) {
        Logger.d(TAG, "Searching items with query: " + query);
        return fileDao.searchItems(query);
    }

    public void getItemByFirestoreId(String firestoreId, OnItemRetrievedListener listener) {
        Logger.d(TAG, "Getting item by Firestore ID: " + firestoreId);
        if (firestoreId == null || firestoreId.isEmpty()) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onSuccess(null);
                }
            });
            return;
        }

        executorService.execute(() -> {
            try {
                FileItem item = fileDao.findByFirestoreId(firestoreId);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(item);
                    }
                });
            } catch (Exception e) {
                Logger.e(TAG, "Error getting item by Firestore ID", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public interface OnOperationCompleteListener {
        void onSuccess(long id);
        void onError(Exception e);
    }

    public interface OnItemRetrievedListener {
        void onSuccess(FileItem item);
        void onError(Exception e);
    }
}
