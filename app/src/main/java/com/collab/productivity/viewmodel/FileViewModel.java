package com.collab.productivity.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.data.repository.FileRepository;
import com.collab.productivity.utils.Logger;
import java.util.ArrayList;
import java.util.List;

public class FileViewModel extends AndroidViewModel {
    private static final String TAG = "FileViewModel";
    private final FileRepository repository;
    private final MutableLiveData<Long> currentFolderId;
    private final MutableLiveData<String> currentPath;
    private final MutableLiveData<String> statusMessage;
    private final MutableLiveData<List<FileItem>> currentFiles;

    public FileViewModel(Application application) {
        super(application);
        repository = new FileRepository(application);
        currentFolderId = new MutableLiveData<>(null);
        currentPath = new MutableLiveData<>("/");
        statusMessage = new MutableLiveData<>();
        currentFiles = new MutableLiveData<>(new ArrayList<>());
        Logger.d(TAG, "FileViewModel initialized");
    }

    public void updateFileList(List<FileItem> files) {
        Logger.d(TAG, "Updating file list with " + files.size() + " items");
        currentFiles.setValue(files);
    }

    public LiveData<List<FileItem>> getCurrentFolderContents() {
        Logger.d(TAG, "Getting current folder contents");
        return currentFiles;
    }

    public void createFolder(String name, String description) {
        Logger.d(TAG, "Creating folder: " + name);
        FileItem folder = new FileItem(name,
                                     currentPath.getValue() + "/" + name,
                                     description,
                                     currentFolderId.getValue(),
                                     true);

        repository.insert(folder, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "Folder created with id: " + id);
                statusMessage.setValue("Folder created successfully");
            }

            @Override
            public void onError(Exception e) {
                Logger.e(TAG, "Error creating folder", e);
                statusMessage.setValue("Error creating folder");
            }
        });
    }

    public void uploadFile(String name, String path, String description, long size, String mimeType) {
        Logger.d(TAG, "Uploading file: " + name);
        FileItem file = new FileItem(name, path, description, currentFolderId.getValue(), false);
        file.setSize(size);
        file.setMimeType(mimeType);

        repository.insert(file, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "File uploaded with id: " + id);
                statusMessage.setValue("File uploaded successfully");
            }

            @Override
            public void onError(Exception e) {
                Logger.e(TAG, "Error uploading file", e);
                statusMessage.setValue("Error uploading file");
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
        repository.update(item, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "Item updated successfully");
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
        repository.delete(item, new FileRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Logger.d(TAG, "Item deleted successfully");
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

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void updatePath(String newPath) {
        Logger.d(TAG, "Updating path to: " + newPath);
        currentPath.setValue(newPath);
    }
}
