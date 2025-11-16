package com.collab.productivity.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.collab.productivity.data.model.Note;
import com.collab.productivity.data.repository.NoteRepository;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void deleteById(long noteId) {
        repository.deleteById(noteId);
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<Note> getNoteById(long noteId) {
        return repository.getNoteById(noteId);
    }

    // Search functionality
    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public LiveData<List<Note>> searchNotesAdvanced(String query) {
        return repository.searchNotesAdvanced(query);
    }

    public LiveData<List<Note>> getNotesByMood(String moodType) {
        return repository.getNotesByMood(moodType);
    }

    public LiveData<List<Note>> getNotesByTag(String tag) {
        return repository.getNotesByTag(tag);
    }

    public LiveData<List<String>> getAllMoodTypes() {
        return repository.getAllMoodTypes();
    }

    public LiveData<List<String>> getAllTags() {
        return repository.getAllTags();
    }
}

