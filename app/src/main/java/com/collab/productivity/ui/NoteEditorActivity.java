package com.collab.productivity.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Note;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class NoteEditorActivity extends AppCompatActivity {
    private static final String TAG = "NoteEditorActivity";
    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_NOTE_TITLE = "note_title";
    public static final String EXTRA_NOTE_CONTENT = "note_content";

    private EditText titleEditText;
    private EditText contentEditText;
    private NoteViewModel noteViewModel;
    private long noteId = -1;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write Note");
        }

        // Initialize views
        titleEditText = findViewById(R.id.note_title);
        contentEditText = findViewById(R.id.note_content);

        // Initialize ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Check if editing existing note
        if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
            noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
            if (noteId != -1) {
                loadNote(noteId);
            }
        }
    }

    private void loadNote(long noteId) {
        noteViewModel.getNoteById(noteId).observe(this, note -> {
            if (note != null) {
                currentNote = note;
                titleEditText.setText(note.getTitle());
                contentEditText.setText(note.getContent());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_save) {
            saveNote();
            return true;
        } else if (itemId == R.id.action_delete && noteId != -1) {
            deleteNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use first line of content as title if title is empty
        if (title.isEmpty() && !content.isEmpty()) {
            String[] lines = content.split("\n");
            title = lines[0].length() > 50 ? lines[0].substring(0, 50) + "..." : lines[0];
        }

        try {
            if (noteId != -1 && currentNote != null) {
                // Update existing note
                currentNote.setTitle(title);
                currentNote.setContent(content);
                noteViewModel.update(currentNote);
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            } else {
                // Create new note
                Note note = new Note(title, content);
                noteViewModel.insert(note);
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (Exception e) {
            Logger.e(TAG, "Error saving note", e);
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote() {
        if (noteId != -1) {
            noteViewModel.deleteById(noteId);
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Auto-save on back press
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (!title.isEmpty() || !content.isEmpty()) {
            saveNote();
        } else {
            super.onBackPressed();
        }
    }
}

