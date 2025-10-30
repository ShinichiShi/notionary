package com.collab.productivity.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FileDetailsAdapter - Displays file metadata in a detailed list view
 */
public class FileDetailsAdapter extends RecyclerView.Adapter<FileDetailsAdapter.ViewHolder> {

    private final Context context;
    private List<FileItem> files;
    private final SimpleDateFormat dateFormat;

    public FileDetailsAdapter(Context context) {
        this.context = context;
        this.files = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    public void setFiles(List<FileItem> files) {
        this.files = files != null ? files : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView nameView;
        private final TextView typeView;
        private final TextView sizeView;
        private final TextView pathView;
        private final TextView createdView;
        private final TextView modifiedView;
        private final TextView descriptionView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.file_icon);
            nameView = itemView.findViewById(R.id.file_name);
            typeView = itemView.findViewById(R.id.file_type);
            sizeView = itemView.findViewById(R.id.file_size);
            pathView = itemView.findViewById(R.id.file_path);
            createdView = itemView.findViewById(R.id.file_created);
            modifiedView = itemView.findViewById(R.id.file_modified);
            descriptionView = itemView.findViewById(R.id.file_description);
        }

        void bind(FileItem file) {
            nameView.setText(file.getName());

            // Special handling for ".." parent navigation
            if (file.getName().equals("..")) {
                iconView.setImageResource(R.drawable.ic_folder);
                typeView.setText("Parent Folder");
                sizeView.setText("");
                pathView.setText("");
                createdView.setVisibility(View.GONE);
                modifiedView.setVisibility(View.GONE);
                descriptionView.setVisibility(View.GONE);
                return;
            }

            // Set icon
            iconView.setImageResource(file.isFolder() ?
                R.drawable.ic_folder : R.drawable.ic_file);

            // Set type
            typeView.setText(file.isFolder() ? "Folder" : "File");

            // Set size
            if (file.isFolder()) {
                sizeView.setText("Folder");
            } else {
                sizeView.setText(formatFileSize(file.getSize()));
            }

            // Set path
            pathView.setText(file.getPath());

            // Set dates
            createdView.setVisibility(View.VISIBLE);
            modifiedView.setVisibility(View.VISIBLE);
            createdView.setText("Created: " + dateFormat.format(file.getCreatedAt()));
            modifiedView.setText("Modified: " + dateFormat.format(file.getModifiedAt()));

            // Set description
            if (file.getDescription() != null && !file.getDescription().isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(file.getDescription());
            } else {
                descriptionView.setVisibility(View.GONE);
            }
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format(Locale.getDefault(), "%.1f %s",
                size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}

