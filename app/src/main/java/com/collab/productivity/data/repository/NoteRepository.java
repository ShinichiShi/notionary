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
}

