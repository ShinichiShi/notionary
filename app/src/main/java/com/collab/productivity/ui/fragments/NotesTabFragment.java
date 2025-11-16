package com.collab.productivity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.ui.NoteEditorActivity;
import com.collab.productivity.ui.adapter.NoteAdapter;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.NoteViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment for displaying and managing notes in a tab
 */
public class NotesTabFragment extends Fragment {
    private static final String TAG = "NotesTabFragment";

    private RecyclerView notesRecyclerView;
    private NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;
    private TextView emptyNotesView;
    private TextInputEditText searchNotesEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "Creating NotesTabFragment view");
        View view = inflater.inflate(R.layout.tab_notes, container, false);

        try {
            // Initialize views
            notesRecyclerView = view.findViewById(R.id.recycler_view_notes);
            emptyNotesView = view.findViewById(R.id.empty_notes_view);
            searchNotesEditText = view.findViewById(R.id.search_notes);

            if (notesRecyclerView == null || emptyNotesView == null || searchNotesEditText == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            // Set up RecyclerView
            setupNotesRecyclerView();

            // Set up search functionality
            setupNotesSearch();

            // Initialize ViewModel
            noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
            Logger.d(TAG, "NoteViewModel initialized");

            // Observe notes data
            observeNotes();

        } catch (Exception e) {
            Logger.e(TAG, "Error creating NotesTabFragment view", e);
        }

        return view;
    }

    private void setupNotesRecyclerView() {
        Logger.d(TAG, "Setting up Notes RecyclerView");
        try {
            notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            noteAdapter = new NoteAdapter(getContext());
            notesRecyclerView.setAdapter(noteAdapter);
            Logger.d(TAG, "Notes RecyclerView setup complete");
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Notes RecyclerView", e);
        }
    }

    private void setupNotesSearch() {
        Logger.d(TAG, "Setting up Notes search functionality");
        try {
            searchNotesEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter notes as user types
                    if (noteAdapter != null) {
                        noteAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not needed
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Notes search", e);
        }
    }

    private void observeNotes() {
        Logger.d(TAG, "Setting up Notes observer");
        try {
            // Observe notes from ViewModel
            noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
                if (notes != null) {
                    Logger.d(TAG, "Received notes update from ViewModel, count: " + notes.size());
                    noteAdapter.setNotes(notes);
                    updateEmptyNotesView(notes.isEmpty());
                } else {
                    Logger.w(TAG, "Received null notes from ViewModel");
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Notes observer", e);
        }
    }

    private void updateEmptyNotesView(boolean isEmpty) {
        if (emptyNotesView != null) {
            emptyNotesView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            notesRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Method to open note editor (can be called from parent)
     */
    public void openNoteEditor() {
        try {
            Intent intent = new Intent(getContext(), NoteEditorActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Logger.e(TAG, "Error opening note editor", e);
        }
    }
}
