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
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.FileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import android.webkit.MimeTypeMap;

/**
 * HomeFragment - Displays file management interface
 * Shows a RecyclerView with files and folders, allows file uploads and folder creation
 */
public class HomeFragment extends Fragment implements FileAdapter.FileClickListener {
    private static final String TAG = "HomeFragment";
    private static final int PICK_FILE_REQUEST = 1;

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private FileViewModel fileViewModel;
    private TextView pathView;
    private TextView emptyView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "Creating HomeFragment view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Initialize views
            recyclerView = view.findViewById(R.id.recycler_view_files);
            pathView = view.findViewById(R.id.current_path);
            emptyView = view.findViewById(R.id.empty_view);
            fab = view.findViewById(R.id.fab_add);

            if (recyclerView == null || pathView == null || emptyView == null || fab == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            // Set up RecyclerView
            setupRecyclerView();

            // Initialize ViewModel
            fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
            Logger.d(TAG, "FileViewModel initialized");

            // Observe current path
            fileViewModel.getCurrentPath().observe(getViewLifecycleOwner(), path -> {
                Logger.d(TAG, "Current path updated: " + path);
                pathView.setText(path);
            });

            // Observe file data
            observeViewModel();

            // Set up FAB
            setupFab();

        } catch (Exception e) {
            Logger.e(TAG, "Error in onCreateView", e);
            Toast.makeText(requireContext(), "Error initializing view", Toast.LENGTH_SHORT).show();
        }

        return view;
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
     * Sets up Floating Action Button (FAB) for creating folders and uploading files
     */
    private void setupFab() {
        Logger.d(TAG, "Setting up FAB");
        fab.setOnClickListener(v -> {
            try {
                PopupMenu popup = new PopupMenu(requireContext(), fab);
                popup.getMenu().add(0, 0, 0, getString(R.string.create_folder));
                popup.getMenu().add(0, 1, 1, getString(R.string.upload_file));

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        showCreateFolderDialog();
                    } else {
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
                        Logger.d(TAG, "Creating folder: " + name);
                        fileViewModel.createFolder(name, description);
                        Toast.makeText(requireContext(), "Folder created: " + name, Toast.LENGTH_SHORT).show();
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
            File destinationFile = new File(requireContext().getFilesDir(), fileName);

            // Copy file to app's private storage
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Logger.d(TAG, "File copied successfully: " + destinationFile.getAbsolutePath());
            }

            String mimeType = requireContext().getContentResolver().getType(uri);
            showFileDescriptionDialog(fileName, destinationFile.getAbsolutePath(),
                                   destinationFile.length(), mimeType);

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
            })
            .setNegativeButton("Skip", (dialog, which) -> {
                fileViewModel.uploadFile(fileName, filePath, "", fileSize, mimeType);
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
        fileViewModel.getCurrentFolderContents().observe(getViewLifecycleOwner(), files -> {
            if (files != null) {
                Logger.d(TAG, "Received files update, count: " + files.size());
                fileAdapter.submitList(files);
                updateEmptyView(files.isEmpty());
            }
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        Logger.d(TAG, "Updating empty view state: " + isEmpty);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.isFolder()) {
            fileViewModel.navigateToFolder(item.getId(),
                fileViewModel.getCurrentPath().getValue() + "/" + item.getName());
        } else {
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
}
