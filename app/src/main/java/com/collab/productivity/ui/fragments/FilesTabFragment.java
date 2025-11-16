package com.collab.productivity.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.collab.productivity.R;
import com.collab.productivity.data.model.FileItem;
import com.collab.productivity.ui.FolderDetailsActivity;
import com.collab.productivity.ui.adapter.FileAdapter;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.FileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

/**
 * Fragment for displaying and managing files in a tab
 */
public class FilesTabFragment extends Fragment implements FileAdapter.FileClickListener {
    private static final String TAG = "FilesTabFragment";
    private static final int PICK_FILE_REQUEST = 1;

    public interface FilesTabListener {
        void onCreateFolder();

        void onUploadFile();
    }

    private FilesTabListener listener;

    // ...existing code...

    public void setListener(FilesTabListener listener) {
        this.listener = listener;
    }

    /**
     * Test method to verify fragment communication works
     */
    public void testCommunication() {
        Logger.d(TAG, "Test communication called - FilesTabFragment is responsive");
        Toast.makeText(getContext(), "FilesTabFragment communication working", Toast.LENGTH_SHORT).show();
    }

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private FileViewModel fileViewModel;
    private TextView pathView;
    private TextView fileCountView;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefresh;
    private TextInputEditText searchFilesEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "Creating FilesTabFragment view");
        View view = inflater.inflate(R.layout.tab_files, container, false);

        try {
            // Initialize views
            recyclerView = view.findViewById(R.id.recycler_view_files);
            pathView = view.findViewById(R.id.current_path);
            fileCountView = view.findViewById(R.id.file_count);
            emptyView = view.findViewById(R.id.empty_view);
            swipeRefresh = view.findViewById(R.id.swipe_refresh);

            // Try to find search_files, but don't fail if it's not found (resource caching issue)
            try {
                searchFilesEditText = view.findViewById(R.id.search_files);
                if (searchFilesEditText == null) {
                    Logger.w(TAG, "search_files view not found - resource caching issue");
                }
            } catch (Exception e) {
                Logger.w(TAG, "Error finding search_files view: " + e.getMessage());
                searchFilesEditText = null;
            }

            if (recyclerView == null || pathView == null || emptyView == null || swipeRefresh == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            Logger.d(TAG, "Views initialized - searchFilesEditText: " + (searchFilesEditText != null));

            // Set up SwipeRefreshLayout
            setupSwipeRefresh();

            // Set up RecyclerView
            setupRecyclerView();

            // Set up search functionality
            setupFilesSearch();

            // Initialize ViewModel
            fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
            Logger.d(TAG, "FileViewModel initialized: " + (fileViewModel != null));

            // Set up path navigation
            setupPathNavigation();

            // Observe data
            observeFiles();

            Logger.d(TAG, "FilesTabFragment fully initialized and ready");

        } catch (Exception e) {
            Logger.e(TAG, "Error creating FilesTabFragment view", e);
        }

        return view;
    }

    private void setupSwipeRefresh() {
        Logger.d(TAG, "Setting up SwipeRefreshLayout");
        swipeRefresh.setOnRefreshListener(() -> {
            Logger.d(TAG, "SwipeRefresh triggered - refreshing files");
            // Reload the current folder contents by triggering an update
            if (fileViewModel.getCurrentFolderContents().getValue() != null) {
                fileViewModel.updateFileList(fileViewModel.getCurrentFolderContents().getValue());
            }
            swipeRefresh.setRefreshing(false);
        });
    }

    private void setupRecyclerView() {
        Logger.d(TAG, "Setting up Files RecyclerView");
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            fileAdapter = new FileAdapter(getContext(), this);
            recyclerView.setAdapter(fileAdapter);
            Logger.d(TAG, "Files RecyclerView setup complete");
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Files RecyclerView", e);
        }
    }

    private void setupFilesSearch() {
        Logger.d(TAG, "Setting up Files search functionality");
        try {
            if (searchFilesEditText == null) {
                Logger.w(TAG, "searchFilesEditText is null, skipping search setup");
                return;
            }

            searchFilesEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter files as user types
                    if (fileAdapter != null) {
                        fileAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not needed
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Files search", e);
        }
    }

    private void setupPathNavigation() {
        pathView.setOnClickListener(v -> {
            // Navigate to parent folder or show folder tree
            Logger.d(TAG, "Path clicked - current path: " + fileViewModel.getCurrentPath().getValue());
        });
    }

    private void observeFiles() {
        Logger.d(TAG, "Setting up Files observer");
        try {
            // Observe current path
            fileViewModel.getCurrentPath().observe(getViewLifecycleOwner(), path -> {
                if (path != null) {
                    pathView.setText(path.isEmpty() ? "/" : path);
                }
            });

            // Observe files from ViewModel - this is the MAIN observer for file list updates
            fileViewModel.getCurrentFolderContents().observe(getViewLifecycleOwner(), files -> {
                if (files != null) {
                    Logger.d(TAG, "Received files update from ViewModel, count: " + files.size());
                    fileAdapter.setFiles(new ArrayList<>(files));
                    updateEmptyView(files.isEmpty());
                    updateFileCount(files.size());
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up Files observer", e);
        }
    }

    private void updateEmptyView(boolean isEmpty) {
        if (emptyView != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void updateFileCount(int count) {
        if (fileCountView != null) {
            String countText = count + (count == 1 ? " item" : " items");
            fileCountView.setText(countText);
        }
    }

    @Override
    public void onItemClick(FileItem item) {
        Logger.d(TAG, "File item clicked: " + item.getName());
        try {
            if (item.isFolder()) {
                if (item.getName().equals("..")) {
                    // Navigate to parent folder
                    fileViewModel.navigateToParent();
                } else {
                    // Navigate into folder - need both folderId and path
                    fileViewModel.navigateToFolder(item.getId(), item.getPath());
                }
            } else {
                // Open file based on type
                openFile(item);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error handling item click", e);
        }
    }

    @Override
    public void onItemLongClick(FileItem item, View view) {
        Logger.d(TAG, "File item long clicked: " + item.getName());
        // Show context menu for rename, delete, etc.
        // Implementation placeholder
    }

    /**
     * Method to show create folder dialog (can be called from parent)
     */
    public void showCreateFolderDialog() {
        try {
            Logger.d(TAG, "Showing create folder dialog");
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_folder, null);
            EditText nameInput = dialogView.findViewById(R.id.folder_name_input);

            if (nameInput == null) {
                Logger.e(TAG, "folder_name_input not found in dialog layout", null);
                Toast.makeText(getContext(), "Error loading create folder dialog", Toast.LENGTH_SHORT).show();
                return;
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.create_folder)
                    .setView(dialogView)
                    .setPositiveButton(R.string.create_folder, (dialog, which) -> {
                        String name = nameInput.getText().toString().trim();
                        Logger.d(TAG, "Creating folder with name: '" + name + "'");
                        if (!name.isEmpty()) {
                            // createFolder requires both name and description
                            fileViewModel.createFolder(name, "");
                            Toast.makeText(getContext(), "Creating folder: " + name, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();

            Logger.d(TAG, "Create folder dialog shown successfully");
        } catch (Exception e) {
            Logger.e(TAG, "Error showing create folder dialog", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to open file picker (can be called from parent)
     */
    public void openFilePicker() {
        try {
            Logger.d(TAG, "Opening file picker");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_file)), PICK_FILE_REQUEST);
                Logger.d(TAG, "File picker launched successfully");
            } else {
                Toast.makeText(getContext(), "No file manager app available", Toast.LENGTH_SHORT).show();
                Logger.e(TAG, "No app available to handle file picking", null);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error opening file picker", e);
            Toast.makeText(getContext(), "Error opening file picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Logger.d(TAG, "File selected: " + uri.toString());
                    uploadFile(uri);
                } else {
                    Logger.e(TAG, "URI is null in onActivityResult", null);
                    Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Logger.d(TAG, "File picker cancelled or failed");
                Toast.makeText(getContext(), "File selection cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFile(Uri uri) {
        try {
            // Extract file information
            String fileName = getFileNameFromUri(uri);
            String mimeType = requireContext().getContentResolver().getType(uri);
            long fileSize = getFileSizeFromUri(uri);

            if (fileName == null || fileName.isEmpty()) {
                Toast.makeText(getContext(), "Could not get file name", Toast.LENGTH_SHORT).show();
                return;
            }

            Logger.d(TAG, "Processing file: " + fileName + " (" + fileSize + " bytes, " + mimeType + ")");

            // Copy file to local storage first
            String localFilePath = copyFileToLocalStorage(uri, fileName);

            if (localFilePath != null) {
                Logger.d(TAG, "File copied to local storage: " + localFilePath);
                fileViewModel.uploadFile(fileName, localFilePath, "", fileSize, mimeType != null ? mimeType : "application/octet-stream");
                Toast.makeText(getContext(), "Uploading " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Logger.e(TAG, "Failed to copy file to local storage", null);
                Toast.makeText(getContext(), "Error copying file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error uploading file", e);
            Toast.makeText(getContext(), "Error uploading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String copyFileToLocalStorage(Uri uri, String fileName) {
        try {
            // Create app's files directory
            java.io.File filesDir = new java.io.File(requireContext().getFilesDir(), "uploads");
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }

            // Create destination file
            java.io.File destFile = new java.io.File(filesDir, fileName);

            // Copy file content
            try (java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destFile)) {

                if (inputStream == null) {
                    Logger.e(TAG, "Could not open input stream from URI", null);
                    return null;
                }

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Logger.d(TAG, "File copied successfully to: " + destFile.getAbsolutePath());
                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error copying file to local storage", e);
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
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
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private long getFileSizeFromUri(Uri uri) {
        try (android.database.Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        }
        return 0;
    }

    private void openFile(FileItem item) {
        try {
            String mimeType = item.getMimeType();
            String filePath = item.getPath();

            if (mimeType == null) {
                // Try to determine MIME type from file extension
                String extension = getFileExtension(item.getName());
                mimeType = getMimeTypeFromExtension(extension);
            }

            Logger.d(TAG, "Opening file: " + item.getName() + " (" + mimeType + ")");

            if (mimeType != null && mimeType.startsWith("image/")) {
                // Open image viewer
                openImageViewer(item);
            } else if (mimeType != null && mimeType.equals("text/plain")) {
                // Open text viewer
                openTextViewer(item);
            } else {
                // Try to open with external app
                openWithExternalApp(item);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error opening file: " + item.getName(), e);
            Toast.makeText(getContext(), "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageViewer(FileItem item) {
        try {
            java.io.File file = new java.io.File(item.getPath());
            if (file.exists()) {
                Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No app available to view images", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "File not found: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error opening image viewer", e);
            Toast.makeText(getContext(), "Error opening image", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTextViewer(FileItem item) {
        try {
            java.io.File file = new java.io.File(item.getPath());
            if (file.exists()) {
                Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "text/plain");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No app available to view text files", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "File not found: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error opening text viewer", e);
            Toast.makeText(getContext(), "Error opening text file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWithExternalApp(FileItem item) {
        try {
            java.io.File file = new java.io.File(item.getPath());
            if (file.exists()) {
                Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, item.getMimeType());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No app available to open this file type", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "File not found: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error opening with external app", e);
            Toast.makeText(getContext(), "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    private String getMimeTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "txt":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }

    private String getRealPathFromUri(Uri uri) {
        if ("content".equals(uri.getScheme())) {
            return uri.toString();
        } else if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
