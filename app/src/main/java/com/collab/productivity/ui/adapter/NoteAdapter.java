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
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView titleView;
        private TextView contentPreviewView;
        private TextView dateView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.note_card);
            titleView = itemView.findViewById(R.id.note_title);
            contentPreviewView = itemView.findViewById(R.id.note_content_preview);
            dateView = itemView.findViewById(R.id.note_date);
        }

        public void bind(Note note) {
            titleView.setText(note.getTitle());

            // Show preview of content (first 100 characters)
            String content = note.getContent();
            if (content != null && content.length() > 100) {
                contentPreviewView.setText(content.substring(0, 100) + "...");
            } else {
                contentPreviewView.setText(content);
            }

            dateView.setText(dateFormat.format(note.getModifiedAt()));

            // Click listener to open editor
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoteEditorActivity.class);
                intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, note.getId());
                context.startActivity(intent);
            });
        }
    }
}

