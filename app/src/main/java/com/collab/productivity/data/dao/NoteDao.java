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
}

