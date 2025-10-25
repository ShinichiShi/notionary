package com.collab.productivity.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileAdapter extends ListAdapter<FileItem, FileAdapter.FileViewHolder> {

    private final Context context;
    private final FileClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface FileClickListener {
        void onItemClick(FileItem item);
        void onItemLongClick(FileItem item, View view);
    }

    public FileAdapter(Context context, FileClickListener listener) {
        super(new DiffUtil.ItemCallback<FileItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getModifiedAt().equals(newItem.getModifiedAt());
            }
        });
        this.context = context;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem item = getItem(position);
        holder.bind(item);
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView nameView;
        private final TextView detailsView;
        private final TextView descriptionView;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.file_icon);
            nameView = itemView.findViewById(R.id.file_name);
            detailsView = itemView.findViewById(R.id.file_details);
            descriptionView = itemView.findViewById(R.id.file_description);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(getItem(position), v);
                    return true;
                }
                return false;
            });
        }

        void bind(FileItem item) {
            nameView.setText(item.getName());
            iconView.setImageResource(item.isFolder() ?
                R.drawable.ic_folder : R.drawable.ic_file);

            String details = item.isFolder() ?
                context.getString(R.string.folder) :
                formatFileSize(item.getSize());
            details += " • " + dateFormat.format(item.getModifiedAt());
            detailsView.setText(details);

            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(item.getDescription());
            } else {
                descriptionView.setVisibility(View.GONE);
            }
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}
