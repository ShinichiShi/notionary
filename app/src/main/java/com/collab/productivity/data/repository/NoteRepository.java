package com.collab.productivity.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.collab.productivity.data.dao.NoteDao;
import com.collab.productivity.data.database.AppDatabase;
import com.collab.productivity.data.model.Note;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    public NoteRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    public void deleteById(long noteId) {
        executorService.execute(() -> noteDao.deleteById(noteId));
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<Note> getNoteById(long noteId) {
        return noteDao.getNoteById(noteId);
    }

    // Search functionality
    public LiveData<List<Note>> searchNotes(String query) {
        String searchQuery = "%" + query + "%";
        return noteDao.searchNotes(searchQuery);
    }

    public LiveData<List<Note>> searchNotesAdvanced(String query) {
        String searchQuery = "%" + query + "%";
        return noteDao.searchNotesAdvanced(searchQuery);
    }

    public LiveData<List<Note>> getNotesByMood(String moodType) {
        return noteDao.getNotesByMood(moodType);
    }

    public LiveData<List<Note>> getNotesByTag(String tag) {
        String tagQuery = "%" + tag + "%";
        return noteDao.getNotesByTag(tagQuery);
    }

    public LiveData<List<String>> getAllMoodTypes() {
        return noteDao.getAllMoodTypes();
    }

    public LiveData<List<String>> getAllTags() {
        return noteDao.getAllTags();
    }
}

