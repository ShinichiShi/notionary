package com.collab.productivity.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.collab.productivity.NotionaryApp;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.data.repository.FileRepository;
import com.collab.productivity.utils.Logger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileViewModel extends AndroidViewModel {
    private static final String TAG = "FileViewModel";
    private final FileRepository repository;
    private final MutableLiveData<Long> currentFolderId;
    private final MutableLiveData<String> currentPath;
    private final ExecutorService executorService;

    public FileViewModel(Application application) {
        super(application);
        repository = new FileRepository(application);
        currentFolderId = new MutableLiveData<>(null);
        currentPath = new MutableLiveData<>("/");
        executorService = Executors.newFixedThreadPool(2);
        Logger.d(TAG, "FileViewModel initialized");
    }

    public LiveData<List<FileItem>> getCurrentFolderContents() {
        Logger.d(TAG, "Getting contents for folder: " + currentFolderId.getValue());
        return repository.getItemsInFolder(currentFolderId.getValue());
    }

    public void createFolder(String name, String description) {
        Logger.d(TAG, "Creating folder: " + name);
        FileItem folder = new FileItem(name,
                                     currentPath.getValue() + "/" + name,
                                     description,
                                     currentFolderId.getValue(),
                                     true);
        executorService.execute(() -> {
            try {
                long id = repository.insert(folder);
                Logger.d(TAG, "Folder created with id: " + id);
            } catch (Exception e) {
                Logger.e(TAG, "Error creating folder", e);
            }
        });
    }

    public void uploadFile(String name, String path, String description, long size, String mimeType) {
        Logger.d(TAG, "Uploading file: " + name);
        FileItem file = new FileItem(name, path, description, currentFolderId.getValue(), false);
        file.setSize(size);
        file.setMimeType(mimeType);
        executorService.execute(() -> {
            try {
                long id = repository.insert(file);
                Logger.d(TAG, "File uploaded with id: " + id);
            } catch (Exception e) {
                Logger.e(TAG, "Error uploading file", e);
            }
        });
    }

    public void navigateToFolder(Long folderId, String folderPath) {
        Logger.d(TAG, "Navigating to folder: " + folderPath);
        currentFolderId.setValue(folderId);
        currentPath.setValue(folderPath);
    }

    public void updateItem(FileItem item) {
        Logger.d(TAG, "Updating item: " + item.getName());
        executorService.execute(() -> {
            try {
                repository.update(item);
                Logger.d(TAG, "Item updated successfully");
            } catch (Exception e) {
                Logger.e(TAG, "Error updating item", e);
            }
        });
    }

    public void deleteItem(FileItem item) {
        Logger.d(TAG, "Deleting item: " + item.getName());
        executorService.execute(() -> {
            try {
                repository.delete(item);
                Logger.d(TAG, "Item deleted successfully");
            } catch (Exception e) {
                Logger.e(TAG, "Error deleting item", e);
            }
        });
    }

    public LiveData<String> getCurrentPath() {
        return currentPath;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
