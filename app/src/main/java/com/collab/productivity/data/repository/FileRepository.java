package com.collab.productivity.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.collab.productivity.NotionaryApp;
import com.collab.productivity.data.dao.FileDao;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.utils.Logger;
import java.util.List;

public class FileRepository {
    private static final String TAG = "FileRepository";
    private final FileDao fileDao;

    public FileRepository(Context context) {
        NotionaryApp app = (NotionaryApp) context.getApplicationContext();
        fileDao = app.getDatabase().fileDao();
        Logger.d(TAG, "FileRepository initialized");
    }

    public LiveData<List<FileItem>> getRootItems() {
        Logger.d(TAG, "Getting root items");
        return fileDao.getRootItems();
    }

    public LiveData<List<FileItem>> getItemsInFolder(Long folderId) {
        Logger.d(TAG, "Getting items in folder: " + folderId);
        return fileDao.getItemsInFolderSorted(folderId);
    }

    public LiveData<FileItem> getItemById(long id) {
        Logger.d(TAG, "Getting item by id: " + id);
        return fileDao.getItemById(id);
    }

    public long insert(FileItem fileItem) {
        Logger.d(TAG, "Inserting item: " + fileItem.getName());
        return fileDao.insert(fileItem);
    }

    public void update(FileItem fileItem) {
        Logger.d(TAG, "Updating item: " + fileItem.getName());
        fileDao.update(fileItem);
    }

    public void delete(FileItem fileItem) {
        Logger.d(TAG, "Deleting item: " + fileItem.getName());
        fileDao.delete(fileItem);
    }

    public void moveItem(long itemId, Long newParentId) {
        Logger.d(TAG, "Moving item " + itemId + " to parent " + newParentId);
        fileDao.moveItem(itemId, newParentId);
    }

    public LiveData<List<FileItem>> searchItems(String query) {
        Logger.d(TAG, "Searching items with query: " + query);
        return fileDao.searchItems(query);
    }
}
