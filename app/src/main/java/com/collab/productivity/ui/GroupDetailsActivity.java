package com.collab.productivity.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.data.model.Group;
import com.collab.productivity.ui.adapter.FileAdapter;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.FileViewModel;
import com.collab.productivity.viewmodel.GroupViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * GroupDetailsActivity - Shows group details and allows file/folder management within the group
 */
public class GroupDetailsActivity extends AppCompatActivity implements FileAdapter.FileClickListener {
    private static final String TAG = "GroupDetailsActivity";
    private static final int PICK_FILE_REQUEST = 1;

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_NAME = "group_name";

    private String groupId;
    private String groupName;
    private Group currentGroup;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private FileViewModel fileViewModel;
    private GroupViewModel groupViewModel;
    private TextView pathView;
    private TextView fileCountView;
    private TextView emptyView;
    private TextView groupDescriptionView;
    private TextView memberCountView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Get group info from intent
        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);

        if (groupId == null || groupName == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        initViewModels();
        loadGroupDetails();
        observeViewModel();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view_files);
        pathView = findViewById(R.id.current_path);
        fileCountView = findViewById(R.id.file_count);
        emptyView = findViewById(R.id.empty_view);
        groupDescriptionView = findViewById(R.id.group_description);
        memberCountView = findViewById(R.id.member_count);
        fab = findViewById(R.id.fab_add);
        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(groupName);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this, this);
        recyclerView.setAdapter(fileAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            Logger.d(TAG, "SwipeRefresh triggered - syncing group files");
            fileViewModel.syncGroupFilesFromFirestore(groupId, new FileViewModel.SyncCallback() {
                @Override
                public void onSuccess() {
                    Logger.d(TAG, "Group files sync completed");
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void onError(String error) {
                    Logger.e(TAG, "Group files sync failed: " + error, null);
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    Toast.makeText(GroupDetailsActivity.this, "Sync error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, fab);
            popup.getMenu().add(0, 0, 0, getString(R.string.create_folder));
            popup.getMenu().add(0, 1, 1, getString(R.string.upload_file));

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    showCreateFolderDialog();
                } else if (item.getItemId() == 1) {
                    openFilePicker();
                }
                return true;
            });

            popup.show();
        });
    }

    private void initViewModels() {
        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        // Set the current group context for file operations
        fileViewModel.setCurrentGroupContext(groupId);
    }

    private void loadGroupDetails() {
        groupViewModel.getGroupById(groupId, new GroupViewModel.GroupCreationCallback() {
            @Override
            public void onSuccess(Group group) {
                currentGroup = group;
                updateGroupInfo(group);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GroupDetailsActivity.this, "Error loading group: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupInfo(Group group) {
        if (groupDescriptionView != null) {
            String desc = group.getDescription();
            if (desc != null && !desc.isEmpty()) {
                groupDescriptionView.setText(desc);
                groupDescriptionView.setVisibility(View.VISIBLE);
            } else {
                groupDescriptionView.setVisibility(View.GONE);
            }
        }

        if (memberCountView != null) {
            int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
            memberCountView.setText(getString(R.string.member_count, memberCount));
        }
    }

    private void observeViewModel() {
        // Observe group files
        fileViewModel.getGroupFiles(groupId).observe(this, files -> {
            if (files != null) {
                Logger.d(TAG, "Received group files update, count: " + files.size());
                fileAdapter.submitList(new ArrayList<>(files));
                updateEmptyView(files.isEmpty());
                updateFileCount(files.size());
            }
        });

        // Observe current path
        fileViewModel.getCurrentPath().observe(this, path -> {
            Logger.d(TAG, "Current path updated: " + path);
            pathView.setText(path);
        });

        // Observe upload progress
        fileViewModel.getUploadProgress().observe(this, progress -> {
            if (progress != null && progress > 0 && progress < 100) {
                Logger.d(TAG, "Upload progress: " + progress + "%");
                Toast.makeText(this, "Uploading: " + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe status messages
        fileViewModel.getStatusMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Logger.d(TAG, "Status message: " + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateFolderDialog() {
        Logger.d(TAG, "Showing create folder dialog for group");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_folder, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.folder_name_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.folder_description_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.create_folder)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                if (!name.isEmpty()) {
                    Logger.d(TAG, "Creating folder in group: " + name);
                    fileViewModel.createFolderInGroup(name, description, groupId);
                    Toast.makeText(this, "Creating folder: " + name, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void openFilePicker() {
        Logger.d(TAG, "Opening file picker for group upload");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_file)), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Logger.d(TAG, "File selected from picker for group");
            handleFileSelection(data.getData());
        }
    }

    private void handleFileSelection(Uri uri) {
        Logger.d(TAG, "Handling file selection for group: " + uri);
        try {
            String fileName = getFileNameFromUri(uri);
            String currentPath = fileViewModel.getCurrentPath().getValue();
            File currentDir = new File(getFilesDir(), "groups/" + groupId + "/" + (currentPath.equals("/") ? "" : currentPath));

            if (!currentDir.exists()) {
                boolean dirsCreated = currentDir.mkdirs();
                Logger.d(TAG, "Creating directory structure: " + currentDir.getAbsolutePath() + ", success: " + dirsCreated);
            }

            File destinationFile = new File(currentDir, fileName);
            Logger.d(TAG, "Destination file path: " + destinationFile.getAbsolutePath());

            // Copy file
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Logger.d(TAG, "File copied successfully: " + destinationFile.getAbsolutePath());

                String mimeType = getContentResolver().getType(uri);
                showFileDescriptionDialog(fileName, destinationFile.getAbsolutePath(),
                    destinationFile.length(), mimeType);
            }

        } catch (Exception e) {
            Logger.e(TAG, "Error handling file selection", e);
            Toast.makeText(this, "Error uploading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showFileDescriptionDialog(String fileName, String filePath, long fileSize, String mimeType) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_file_description, null);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.file_description_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Add File Description")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String description = descriptionInput.getText().toString().trim();
                fileViewModel.uploadFileToGroup(fileName, filePath, description, fileSize, mimeType, groupId);
                Toast.makeText(this, "Uploading file...", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                fileViewModel.uploadFileToGroup(fileName, filePath, "", fileSize, mimeType, groupId);
                Toast.makeText(this, "Uploading file...", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateEmptyView(boolean isEmpty) {
        if (emptyView != null && recyclerView != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateFileCount(int count) {
        if (fileCountView != null) {
            String text = count + (count == 1 ? " item" : " items");
            fileCountView.setText(text);
        }
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.getName().equals("..")) {
            Logger.d(TAG, "Navigating to parent folder");
            fileViewModel.navigateToParent();
        } else if (item.isFolder()) {
            Logger.d(TAG, "Navigating into folder: " + item.getName());
            fileViewModel.navigateToFolder(item.getId(), item.getPath());
        } else {
            openFile(item);
        }
    }

    private void openFile(FileItem item) {
        try {
            File file = new File(item.getPath());
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = item.getMimeType();
            if (mimeType == null) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No app found to open this file type", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(FileItem item, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 0, 0, R.string.rename);
        popup.getMenu().add(0, 1, 1, R.string.delete);

        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case 0:
                    showRenameDialog(item);
                    return true;
                case 1:
                    showDeleteConfirmation(item);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void showRenameDialog(FileItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename_item, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.item_name_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.item_description_input);

        nameInput.setText(item.getName());
        descriptionInput.setText(item.getDescription());

        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.rename))
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String newName = nameInput.getText().toString().trim();
                String newDescription = descriptionInput.getText().toString().trim();
                if (!newName.isEmpty()) {
                    item.setName(newName);
                    item.setDescription(newDescription);
                    fileViewModel.updateItem(item);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showDeleteConfirmation(FileItem item) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete))
            .setMessage("Are you sure you want to delete " + item.getName() + "?")
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                fileViewModel.deleteItem(item);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

