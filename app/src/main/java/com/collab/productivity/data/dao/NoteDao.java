package com.collab.productivity.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.collab.productivity.data.model.Note;
import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY modified_at DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<Note> getNoteById(long noteId);

    @Query("SELECT * FROM notes ORDER BY modified_at DESC")
    List<Note> getAllNotesSync();

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteById(long noteId);

    // Search functionality
    @Query("SELECT * FROM notes WHERE title LIKE :searchQuery OR content LIKE :searchQuery ORDER BY modified_at DESC")
    LiveData<List<Note>> searchNotes(String searchQuery);

    @Query("SELECT * FROM notes WHERE title LIKE :searchQuery OR content LIKE :searchQuery OR tags LIKE :searchQuery OR mood_type LIKE :searchQuery ORDER BY modified_at DESC")
    LiveData<List<Note>> searchNotesAdvanced(String searchQuery);

    @Query("SELECT * FROM notes WHERE mood_type = :moodType ORDER BY modified_at DESC")
    LiveData<List<Note>> getNotesByMood(String moodType);

    @Query("SELECT * FROM notes WHERE tags LIKE :tag ORDER BY modified_at DESC")
    LiveData<List<Note>> getNotesByTag(String tag);

    @Query("SELECT DISTINCT mood_type FROM notes WHERE mood_type IS NOT NULL AND mood_type != ''")
    LiveData<List<String>> getAllMoodTypes();

    @Query("SELECT DISTINCT tags FROM notes WHERE tags IS NOT NULL AND tags != ''")
    LiveData<List<String>> getAllTags();
}

