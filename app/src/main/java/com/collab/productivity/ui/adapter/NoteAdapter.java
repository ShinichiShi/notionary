package com.collab.productivity.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Note;
import com.collab.productivity.ui.NoteEditorActivity;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes = new ArrayList<>();
    private List<Note> notesFiltered = new ArrayList<>();
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public NoteAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesFiltered.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notesFiltered.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        this.notesFiltered = new ArrayList<>(notes);
        notifyDataSetChanged();
    }

    public void filter(String searchText) {
        notesFiltered.clear();
        if (searchText.isEmpty()) {
            notesFiltered.addAll(notes);
        } else {
            String searchLower = searchText.toLowerCase();
            for (Note note : notes) {
                if ((note.getTitle() != null && note.getTitle().toLowerCase().contains(searchLower)) ||
                    (note.getContent() != null && note.getContent().toLowerCase().contains(searchLower)) ||
                    (note.getTags() != null && note.getTags().toLowerCase().contains(searchLower)) ||
                    (note.getMoodType() != null && note.getMoodType().toLowerCase().contains(searchLower))) {
                    notesFiltered.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView titleView;
        private TextView contentPreviewView;
        private TextView dateView;
        private TextView moodTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.note_card);
            titleView = itemView.findViewById(R.id.note_title);
            contentPreviewView = itemView.findViewById(R.id.note_content_preview);
            dateView = itemView.findViewById(R.id.note_date);
            moodTextView = itemView.findViewById(R.id.text_note_mood);
        }

        public void bind(Note note) {
            // Set title - use display title which handles empty titles
            titleView.setText(note.getDisplayTitle());

            // Show preview of content (first 100 characters)
            String content = note.getContent();
            if (content != null && content.length() > 100) {
                contentPreviewView.setText(content.substring(0, 100) + "...");
            } else {
                contentPreviewView.setText(content);
            }

            // Set date
            if (note.getModifiedAt() != null) {
                dateView.setText(dateFormat.format(note.getModifiedAt()));
            } else if (note.getCreatedAt() != null) {
                dateView.setText(dateFormat.format(note.getCreatedAt()));
            }

            // Set mood display
            if (moodTextView != null) {
                String moodDisplay = note.getMoodDisplay();
                if (moodDisplay != null && !moodDisplay.trim().isEmpty()) {
                    moodTextView.setText(moodDisplay);
                    moodTextView.setVisibility(View.VISIBLE);
                } else {
                    moodTextView.setVisibility(View.GONE);
                }
            }

            // Click listener to open editor
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoteEditorActivity.class);
                intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, note.getId());
                context.startActivity(intent);
            });
        }
    }
}

