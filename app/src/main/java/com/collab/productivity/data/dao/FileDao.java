package com.collab.productivity.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.*;
import com.collab.productivity.data.model.FileItem;
import java.util.List;

@Dao
public interface FileDao {
    @Query("SELECT * FROM files WHERE parent_folder_id IS NULL")
    LiveData<List<FileItem>> getRootItems();

    @Query("SELECT * FROM files WHERE parent_folder_id IS NULL ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    LiveData<List<FileItem>> getRootItemsSorted();

    @Query("SELECT * FROM files WHERE parent_folder_id = :folderId")
    LiveData<List<FileItem>> getItemsInFolder(Long folderId);

    @Query("SELECT * FROM files WHERE id = :id")
    LiveData<FileItem> getItemById(long id);

    @Query("SELECT * FROM files ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    LiveData<List<FileItem>> getAllFiles();

    @Insert
    long insert(FileItem fileItem);

    @Update
    void update(FileItem fileItem);

    @Delete
    void delete(FileItem fileItem);

    @Query("SELECT * FROM files WHERE parent_folder_id = :folderId ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    LiveData<List<FileItem>> getItemsInFolderSorted(Long folderId);

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%'")
    LiveData<List<FileItem>> searchItems(String query);

    @Query("UPDATE files SET parent_folder_id = :newParentId WHERE id = :itemId")
    void moveItem(long itemId, Long newParentId);

    @Query("SELECT COUNT(*) FROM files WHERE parent_folder_id = :folderId")
    int getItemCount(long folderId);

    @Query("SELECT * FROM files WHERE firestore_id = :firestoreId LIMIT 1")
    FileItem findByFirestoreId(String firestoreId);

    @Query("SELECT * FROM files WHERE group_id = :groupId ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    LiveData<List<FileItem>> getGroupFiles(String groupId);

    @Query("SELECT * FROM files WHERE group_id = :groupId ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    List<FileItem> getGroupFilesSync(String groupId);

    @Query("SELECT * FROM files WHERE group_id = :groupId ORDER BY " +
           "CASE WHEN is_folder = 1 THEN 0 ELSE 1 END, " +
           "name COLLATE NOCASE ASC")
    PagingSource<Integer, FileItem> getGroupFilesPaging(String groupId);
}
