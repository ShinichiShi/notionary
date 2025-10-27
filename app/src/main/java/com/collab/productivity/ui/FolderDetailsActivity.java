package com.collab.productivity.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.ui.adapter.FileDetailsAdapter;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.FileViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * FolderDetailsActivity - Displays detailed metadata about a folder and its contents
 */
public class FolderDetailsActivity extends AppCompatActivity {
    private static final String TAG = "FolderDetailsActivity";
    public static final String EXTRA_FOLDER_ID = "folder_id";
    public static final String EXTRA_FOLDER_NAME = "folder_name";
    public static final String EXTRA_FOLDER_PATH = "folder_path";

    private FileViewModel fileViewModel;
    private TextView folderNameView;
    private TextView folderPathView;
    private TextView folderDescriptionView;
    private TextView folderCreatedView;
    private TextView folderModifiedView;
    private TextView folderItemCountView;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private FileDetailsAdapter adapter;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_details);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        folderNameView = findViewById(R.id.folder_name);
        folderPathView = findViewById(R.id.folder_path);
        folderDescriptionView = findViewById(R.id.folder_description);
        folderCreatedView = findViewById(R.id.folder_created);
        folderModifiedView = findViewById(R.id.folder_modified);
        folderItemCountView = findViewById(R.id.folder_item_count);
        emptyView = findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.files_recycler_view);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileDetailsAdapter(this);
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);

        // Get folder info from intent
        long folderId = getIntent().getLongExtra(EXTRA_FOLDER_ID, -1);
        String folderName = getIntent().getStringExtra(EXTRA_FOLDER_NAME);
        String folderPath = getIntent().getStringExtra(EXTRA_FOLDER_PATH);

        if (folderId == -1 || folderPath == null) {
            // Show root folder details
            loadRootFolderDetails();
        } else {
            // Load folder details
            loadFolderDetails(folderId, folderName, folderPath);
        }
    }

    private void loadRootFolderDetails() {
        Logger.d(TAG, "Loading root folder details");
        folderNameView.setText("Root Folder");
        folderPathView.setText("/");
        folderDescriptionView.setText("Root directory");
        folderCreatedView.setText("N/A");
        folderModifiedView.setText("N/A");

        // Observe root items
        fileViewModel.getAllFiles().observe(this, files -> {
            if (files != null) {
                // Filter root items (items with no parent folder)
                ArrayList<FileItem> rootItems = new ArrayList<>();
                for (FileItem file : files) {
                    if (file.getParentFolderId() == null) {
                        rootItems.add(file);
                    }
                }

                folderItemCountView.setText(rootItems.size() + (rootItems.size() == 1 ? " item" : " items"));
                adapter.setFiles(rootItems);

                if (rootItems.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadFolderDetails(long folderId, String folderName, String folderPath) {
        Logger.d(TAG, "Loading folder details for ID: " + folderId);

        // First, get the folder itself to show its metadata
        fileViewModel.getAllFiles().observe(this, allFiles -> {
            if (allFiles != null) {
                FileItem folder = null;
                ArrayList<FileItem> folderContents = new ArrayList<>();

                // Find the folder and its contents
                for (FileItem file : allFiles) {
                    if (file.getId() == folderId) {
                        folder = file;
                    }
                    if (file.getParentFolderId() != null && file.getParentFolderId() == folderId) {
                        folderContents.add(file);
                    }
                }

                // Update folder metadata
                if (folder != null) {
                    folderNameView.setText(folder.getName());
                    folderPathView.setText(folder.getPath());
                    folderDescriptionView.setText(
                        folder.getDescription() != null && !folder.getDescription().isEmpty()
                            ? folder.getDescription()
                            : "No description"
                    );
                    folderCreatedView.setText(dateFormat.format(folder.getCreatedAt()));
                    folderModifiedView.setText(dateFormat.format(folder.getModifiedAt()));
                } else {
                    folderNameView.setText(folderName);
                    folderPathView.setText(folderPath);
                    folderDescriptionView.setText("No description");
                    folderCreatedView.setText("N/A");
                    folderModifiedView.setText("N/A");
                }

                // Update files list
                folderItemCountView.setText(folderContents.size() +
                    (folderContents.size() == 1 ? " item" : " items"));
                adapter.setFiles(folderContents);

                if (folderContents.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}

