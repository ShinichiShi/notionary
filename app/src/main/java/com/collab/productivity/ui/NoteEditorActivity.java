package com.collab.productivity.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Note;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.Locale;

public class NoteEditorActivity extends AppCompatActivity {
    private static final String TAG = "NoteEditorActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_NOTE_TITLE = "note_title";
    public static final String EXTRA_NOTE_CONTENT = "note_content";

    private EditText titleEditText;
    private EditText contentEditText;
    private NoteViewModel noteViewModel;
    private long noteId = -1;
    private Note currentNote;

    // Speech recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private MenuItem voiceMenuItem;

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

        // Initialize speech recognizer
        initializeSpeechRecognizer();

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
        voiceMenuItem = menu.findItem(R.id.action_voice_input);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_voice_input) {
            toggleVoiceInput();
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

    // Speech Recognition Methods
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Logger.d(TAG, "Ready for speech");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Logger.d(TAG, "Speech started");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Audio level changed
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Partial audio data received
                }

                @Override
                public void onEndOfSpeech() {
                    Logger.d(TAG, "Speech ended");
                    isListening = false;
                    updateVoiceIcon();
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    Logger.d(TAG, "Speech recognition error: " + errorMessage);
                    Toast.makeText(NoteEditorActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    isListening = false;
                    updateVoiceIcon();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        appendTextToContent(recognizedText);
                    }
                    isListening = false;
                    updateVoiceIcon();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Partial results available
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Reserved for future use
                }
            });
        } else {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVoiceInput() {
        if (!isListening) {
            startListening();
        } else {
            stopListening();
        }
    }

    private void startListening() {
        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        if (speechRecognizer == null) {
            initializeSpeechRecognizer();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
            updateVoiceIcon();
            Toast.makeText(this, "Listening... Speak now", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e(TAG, "Error starting speech recognition", e);
            Toast.makeText(this, "Error starting voice input", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
            updateVoiceIcon();
        }
    }

    private void appendTextToContent(String text) {
        String currentContent = contentEditText.getText().toString();
        if (!currentContent.isEmpty() && !currentContent.endsWith(" ") && !currentContent.endsWith("\n")) {
            currentContent += " ";
        }
        contentEditText.setText(currentContent + text);
        contentEditText.setSelection(contentEditText.getText().length());
    }

    private void updateVoiceIcon() {
        if (voiceMenuItem != null) {
            runOnUiThread(() -> {
                if (isListening) {
                    voiceMenuItem.setIcon(R.drawable.ic_mic_active);
                } else {
                    voiceMenuItem.setIcon(R.drawable.ic_mic);
                }
            });
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

