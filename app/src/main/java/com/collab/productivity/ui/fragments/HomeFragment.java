package com.collab.productivity.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.ui.adapter.FileAdapter;
import com.collab.productivity.ui.adapter.NoteAdapter;
import com.collab.productivity.ui.NoteEditorActivity;
import com.collab.productivity.ui.FolderDetailsActivity;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.FileViewModel;
import com.collab.productivity.viewmodel.NoteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import android.webkit.MimeTypeMap;
import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * HomeFragment - Displays file management interface
 * Shows a RecyclerView with files and folders, allows file uploads and folder creation
 */
public class HomeFragment extends Fragment implements FileAdapter.FileClickListener {
    private static final String TAG = "HomeFragment";
    private static final int PICK_FILE_REQUEST = 1;

    private RecyclerView recyclerView;
    private RecyclerView notesRecyclerView;
    private FileAdapter fileAdapter;
    private NoteAdapter noteAdapter;
    private FileViewModel fileViewModel;
    private NoteViewModel noteViewModel;
    private TextView pathView;
    private TextView fileCountView;
    private TextView emptyView;
    private TextView emptyNotesView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "Creating HomeFragment view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Initialize views
            recyclerView = view.findViewById(R.id.recycler_view_files);
            notesRecyclerView = view.findViewById(R.id.recycler_view_notes);
            pathView = view.findViewById(R.id.current_path);
            fileCountView = view.findViewById(R.id.file_count);
            emptyView = view.findViewById(R.id.empty_view);
            emptyNotesView = view.findViewById(R.id.empty_notes_view);
            fab = view.findViewById(R.id.fab_add);
            swipeRefresh = view.findViewById(R.id.swipe_refresh);
            if (recyclerView == null || notesRecyclerView == null || pathView == null ||
                emptyView == null || emptyNotesView == null || fab == null || swipeRefresh == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            // Set up SwipeRefreshLayout
            setupSwipeRefresh();

            // Set up RecyclerViews
            setupRecyclerView();
            setupNotesRecyclerView();

            // Initialize ViewModels
            fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
            noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
            Logger.d(TAG, "ViewModels initialized");

            // Set up FAB
            setupFab();

            // Observe ViewModel data first
            observeViewModel();

            // Debug: Log all files in database
            fileViewModel.getAllFiles().observe(getViewLifecycleOwner(), allFiles -> {
                if (allFiles != null) {
                    Logger.d(TAG, "=== DEBUG: Total files in database: " + allFiles.size() + " ===");
                    for (FileItem file : allFiles) {
                        Logger.d(TAG, "  - " + (file.isFolder() ? "[DIR] " : "[FILE] ") +
                                file.getName() + " (ID: " + file.getId() +
                                ", ParentID: " + file.getParentFolderId() +
                                ", Path: " + file.getPath() + ")");
                    }
                }
            });

            // Observe current path changes
            fileViewModel.getCurrentPath().observe(getViewLifecycleOwner(), path -> {
                Logger.d(TAG, "Current path updated: " + path);
                pathView.setText(path);
            });

            // Set up click listener for path view to show folder details
            pathView.setOnClickListener(v -> {
                openFolderDetails();
            });

            // Trigger initial sync from Firestore in background
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) {
                    fileViewModel.syncFilesFromFirestore(new FileViewModel.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.d(TAG, "Initial Firestore sync completed");
                        }

                        @Override
                        public void onError(String error) {
                            Logger.e(TAG, "Initial Firestore sync failed: " + error, null);
                        }
                    });
                }
            }, 1000);

        } catch (Exception e) {
            Logger.e(TAG, "Error in onCreateView", e);
            Toast.makeText(requireContext(), "Error initializing view", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            Logger.d(TAG, "SwipeRefresh triggered - syncing from Firestore");

            // Sync from Firestore
            fileViewModel.syncFilesFromFirestore(new FileViewModel.SyncCallback() {
                @Override
                public void onSuccess() {
                    Logger.d(TAG, "Firestore sync completed");
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void onError(String error) {
                    Logger.e(TAG, "Firestore sync failed: " + error, null);
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Sync error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    /**
     * Sets up RecyclerView with layout manager and adapter
     */
    private void setupRecyclerView() {
        Logger.d(TAG, "Setting up RecyclerView");
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            fileAdapter = new FileAdapter(requireContext(), this);
            recyclerView.setAdapter(fileAdapter);
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    /**
     * Sets up Notes RecyclerView with layout manager and adapter
     */
    private void setupNotesRecyclerView() {
        Logger.d(TAG, "Setting up Notes RecyclerView");
        try {
            notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            noteAdapter = new NoteAdapter(requireContext());
            notesRecyclerView.setAdapter(noteAdapter);
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Notes RecyclerView", e);
        }
    }

    /**
     * Sets up Floating Action Button (FAB) for creating folders and uploading files
     */
    private void setupFab() {
        Logger.d(TAG, "Setting up FAB");
        fab.setOnClickListener(v -> {
            try {
                PopupMenu popup = new PopupMenu(requireContext(), fab);
                popup.getMenu().add(0, 0, 0, getString(R.string.write_note));
                popup.getMenu().add(0, 1, 1, getString(R.string.create_folder));
                popup.getMenu().add(0, 2, 2, getString(R.string.upload_file));

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        openNoteEditor();
                    } else if (item.getItemId() == 1) {
                        showCreateFolderDialog();
                    } else if (item.getItemId() == 2) {
                        openFilePicker();
                    }
                    return true;
                });

                popup.show();
            } catch (Exception e) {
                Logger.e(TAG, "Error showing FAB menu", e);
            }
        });
    }

    /**
     * Opens the note editor to create a new note
     */
    private void openNoteEditor() {
        Intent intent = new Intent(requireContext(), NoteEditorActivity.class);
        startActivity(intent);
    }

    /**
     * Shows dialog for creating a new folder
     */
    private void showCreateFolderDialog() {
        Logger.d(TAG, "Showing create folder dialog");
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_folder, null);
            TextInputEditText nameInput = dialogView.findViewById(R.id.folder_name_input);
            TextInputEditText descriptionInput = dialogView.findViewById(R.id.folder_description_input);

            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.create_folder)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Logger.d(TAG, "Creating folder: " + name + " in path: " +
                                fileViewModel.getCurrentPath().getValue());
                        fileViewModel.createFolder(name, description);
                        Toast.makeText(requireContext(), "Creating folder: " + name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        } catch (Exception e) {
            Logger.e(TAG, "Error showing create folder dialog", e);
        }
    }

    /**
     * Opens file picker to select a file for upload
     */
    private void openFilePicker() {
        Logger.d(TAG, "Opening file picker");
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_file)),
                                PICK_FILE_REQUEST);
        } catch (Exception e) {
            Logger.e(TAG, "Error opening file picker", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Logger.d(TAG, "File selected from picker");
            handleFileSelection(data.getData());
        }
    }

    /**
     * Handles the file selection from file picker
     * @param uri - URI of the selected file
     */
    private void handleFileSelection(Uri uri) {
        Logger.d(TAG, "Handling file selection: " + uri);
        try {
            String fileName = getFileNameFromUri(uri);
            String currentPath = fileViewModel.getCurrentPath().getValue();
            File currentDir = new File(requireContext().getFilesDir(), currentPath.equals("/") ? "" : currentPath);
            File destinationFile = new File(currentDir, fileName);

            // Copy file to current directory
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Logger.d(TAG, "File copied successfully: " + destinationFile.getAbsolutePath());

                // Immediately update the UI after successful file copy
                String mimeType = requireContext().getContentResolver().getType(uri);
                showFileDescriptionDialog(fileName, destinationFile.getAbsolutePath(),
                                     destinationFile.length(), mimeType);
            }

        } catch (Exception e) {
            Logger.e(TAG, "Error handling file selection", e);
            Toast.makeText(requireContext(), "Error uploading file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows dialog to add description for the uploaded file
     * @param fileName - Name of the file
     * @param filePath - Path where the file is saved
     * @param fileSize - Size of the file
     * @param mimeType - MIME type of the file
     */
    private void showFileDescriptionDialog(String fileName, String filePath, long fileSize, String mimeType) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_file_description, null);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.file_description_input);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add File Description")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String description = descriptionInput.getText().toString().trim();
                fileViewModel.uploadFile(fileName, filePath, description, fileSize, mimeType);
                Toast.makeText(requireContext(), "Uploading file...", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                fileViewModel.uploadFile(fileName, filePath, "", fileSize, mimeType);
                Toast.makeText(requireContext(), "Uploading file...", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    /**
     * Retrieves file name from URI
     * @param uri - URI of the file
     * @return - File name
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = requireContext().getContentResolver()
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

    /**
     * Observes ViewModel data changes and updates UI
     */
    private void observeViewModel() {
        Logger.d(TAG, "Setting up ViewModel observation");

        // Observe files from ViewModel - this is the MAIN observer for file list updates
        fileViewModel.getCurrentFolderContents().observe(getViewLifecycleOwner(), files -> {
            if (files != null) {
                Logger.d(TAG, "Received files update from ViewModel, count: " + files.size());
                fileAdapter.submitList(new ArrayList<>(files));
                updateEmptyView(files.isEmpty());
                updateFileCount(files.size());
            }
        });


        // Observe notes
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            if (notes != null) {
                Logger.d(TAG, "Received notes update, count: " + notes.size());
                noteAdapter.setNotes(notes);
                updateEmptyNotesView(notes.isEmpty());
            }
        });

        // Observe upload progress
        fileViewModel.getUploadProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && progress > 0 && progress < 100) {
                Logger.d(TAG, "Upload progress: " + progress + "%");
                // Show progress in UI
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Uploading: " + progress + "%", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe status messages
        fileViewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Logger.d(TAG, "Status message: " + message);
                if (isAdded()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void updateEmptyNotesView(boolean isEmpty) {
        if (emptyNotesView != null && notesRecyclerView != null) {
            emptyNotesView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            notesRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.getName().equals("..")) {
            // Navigate to parent directory
            Logger.d(TAG, "Navigating to parent folder");
            fileViewModel.navigateToParent();
        } else if (item.isFolder()) {
            // Navigate into folder
            Logger.d(TAG, "Navigating into folder: " + item.getName() + " (ID: " + item.getId() + ")");
            fileViewModel.navigateToFolder(item.getId(), item.getPath());
        } else {
            // Open file
            openFile(item);
        }
    }

    /**
     * Opens the selected file
     * @param item - FileItem representing the file to be opened
     */
    private void openFile(FileItem item) {
        try {
            File file = new File(item.getPath());
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
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

            // Check if there's an app that can handle this file type
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No app found to open this file type", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(FileItem item, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(0, 0, 0, R.string.rename);
        popup.getMenu().add(0, 1, 1, R.string.move);
        popup.getMenu().add(0, 2, 2, R.string.delete);

        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case 0:
                    showRenameDialog(item);
                    return true;
                case 1:
                    // TODO: Implement move functionality
                    return true;
                case 2:
                    showDeleteConfirmation(item);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    /**
     * Shows dialog to rename a file or folder
     * @param item - FileItem representing the file or folder
     */
    private void showRenameDialog(FileItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename_item, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.item_name_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.item_description_input);

        nameInput.setText(item.getName());
        descriptionInput.setText(item.getDescription());

        new MaterialAlertDialogBuilder(requireContext())
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

    /**
     * Shows confirmation dialog for deleting a file or folder
     * @param item - FileItem representing the file or folder
     */
    private void showDeleteConfirmation(FileItem item) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete))
            .setMessage("Are you sure you want to delete " + item.getName() + "?")
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                fileViewModel.deleteItem(item);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    /**
     * Opens folder details activity to show metadata and files
     */
    private void openFolderDetails() {
        Logger.d(TAG, "Opening folder details");
        Intent intent = new Intent(requireContext(), FolderDetailsActivity.class);

        // Get current folder info from ViewModel
        Long currentFolderId = fileViewModel.getCurrentFolderId().getValue();
        String currentPath = fileViewModel.getCurrentPath().getValue();

        if (currentFolderId != null && currentPath != null) {
            // Get folder name from path
            String folderName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
            if (folderName.isEmpty()) {
                folderName = "Root";
            }

            intent.putExtra(FolderDetailsActivity.EXTRA_FOLDER_ID, currentFolderId);
            intent.putExtra(FolderDetailsActivity.EXTRA_FOLDER_NAME, folderName);
            intent.putExtra(FolderDetailsActivity.EXTRA_FOLDER_PATH, currentPath);
        } else {
            // Root folder - pass special indicators
            intent.putExtra(FolderDetailsActivity.EXTRA_FOLDER_ID, -1L);
            intent.putExtra(FolderDetailsActivity.EXTRA_FOLDER_PATH, "/");
        }

        startActivity(intent);
    }
}

