package com.collab.productivity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.collab.productivity.R;
import com.collab.productivity.ui.NoteEditorActivity;
import com.collab.productivity.ui.adapter.HomeTabsAdapter;
import com.collab.productivity.utils.Logger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * HomeFragment - Main fragment with tabs for Notes and Files
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomeTabsAdapter tabsAdapter;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "Creating HomeFragment view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Initialize views
            tabLayout = view.findViewById(R.id.tab_layout);
            viewPager = view.findViewById(R.id.view_pager);
            fab = view.findViewById(R.id.fab_add);

            if (tabLayout == null || viewPager == null || fab == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            // Set up ViewPager with tabs
            setupTabs();

            // Set up FAB
            setupFab();

        } catch (Exception e) {
            Logger.e(TAG, "Error creating HomeFragment view", e);
        }

        return view;
    }

    private void setupTabs() {
        Logger.d(TAG, "Setting up tabs");
        try {
            // Create adapter
            tabsAdapter = new HomeTabsAdapter(requireActivity());
            viewPager.setAdapter(tabsAdapter);

            // Connect TabLayout with ViewPager2
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(tabsAdapter.getTabTitle(position));
            }).attach();

            // Add page change callback to ensure fragments are initialized
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    Logger.d(TAG, "Page selected: " + position);
                    // Force fragment creation if needed
                    if (position == HomeTabsAdapter.TAB_FILES && tabsAdapter.getFilesTabFragment() == null) {
                        Logger.d(TAG, "FilesTabFragment not created yet, forcing ViewPager to create it");
                    }
                }
            });

            Logger.d(TAG, "Tabs setup complete");
        } catch (Exception e) {
            Logger.e(TAG, "Error setting up tabs", e);
        }
    }

    private void setupFab() {
        Logger.d(TAG, "Setting up FAB");
        fab.setOnClickListener(v -> {
            try {
                PopupMenu popup = new PopupMenu(requireContext(), fab);
                popup.getMenu().add(0, 0, 0, getString(R.string.write_note));
                popup.getMenu().add(0, 1, 1, getString(R.string.create_folder));
                popup.getMenu().add(0, 2, 2, getString(R.string.upload_file));
                popup.getMenu().add(0, 3, 3, "Test Communication"); // Temporary test

                popup.setOnMenuItemClickListener(item -> {
                    int currentTab = viewPager.getCurrentItem();
                    Logger.d(TAG, "FAB menu item clicked: " + item.getItemId() + ", current tab: " + currentTab);

                    if (item.getItemId() == 0) {
                        // Write Note - always available
                        Logger.d(TAG, "Opening note editor");
                        openNoteEditor();
                    } else if (item.getItemId() == 1) {
                        // Create Folder - switch to Files tab if needed
                        Logger.d(TAG, "Create folder requested");
                        if (currentTab != HomeTabsAdapter.TAB_FILES) {
                            viewPager.setCurrentItem(HomeTabsAdapter.TAB_FILES, true);
                        }
                        // Small delay to ensure tab switch completes
                        viewPager.post(() -> createFolder());
                    } else if (item.getItemId() == 2) {
                        // Upload File - switch to Files tab if needed
                        Logger.d(TAG, "Upload file requested");
                        if (currentTab != HomeTabsAdapter.TAB_FILES) {
                            viewPager.setCurrentItem(HomeTabsAdapter.TAB_FILES, true);
                        }
                        // Small delay to ensure tab switch completes
                        viewPager.post(() -> uploadFile());
                    } else if (item.getItemId() == 3) {
                        // Test Communication
                        Logger.d(TAG, "Test communication requested");
                        FilesTabFragment filesFragment = getFilesTabFragment();
                        if (filesFragment != null) {
                            filesFragment.testCommunication();
                        } else {
                            Toast.makeText(getContext(), "FilesTabFragment not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                });

                popup.show();
            } catch (Exception e) {
                Logger.e(TAG, "Error showing FAB menu", e);
            }
        });
    }

    private void openNoteEditor() {
        try {
            Intent intent = new Intent(getContext(), NoteEditorActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Logger.e(TAG, "Error opening note editor", e);
        }
    }

    private void createFolder() {
        try {
            Logger.d(TAG, "createFolder() called from FAB");
            FilesTabFragment filesFragment = getFilesTabFragment();
            if (filesFragment != null) {
                Logger.d(TAG, "FilesTabFragment found, showing create folder dialog");
                filesFragment.showCreateFolderDialog();
            } else {
                Logger.w(TAG, "FilesTabFragment not found");
                Toast.makeText(getContext(), "Files tab not ready, please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error creating folder", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private FilesTabFragment getFilesTabFragment() {
        // Try adapter first
        FilesTabFragment fragment = tabsAdapter.getFilesTabFragment();
        if (fragment != null) {
            Logger.d(TAG, "Found FilesTabFragment from adapter");
            return fragment;
        }

        // Fallback: try to find through fragment manager
        try {
            String fragmentTag = "f" + HomeTabsAdapter.TAB_FILES;
            Fragment f = getChildFragmentManager().findFragmentByTag(fragmentTag);
            if (f instanceof FilesTabFragment) {
                Logger.d(TAG, "Found FilesTabFragment from fragment manager");
                return (FilesTabFragment) f;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error finding fragment through fragment manager", e);
        }

        Logger.w(TAG, "FilesTabFragment not found through any method");
        return null;
    }

    private void uploadFile() {
        try {
            Logger.d(TAG, "uploadFile() called from FAB");
            FilesTabFragment filesFragment = getFilesTabFragment();
            if (filesFragment != null) {
                Logger.d(TAG, "FilesTabFragment found, opening file picker");
                filesFragment.openFilePicker();
            } else {
                Logger.w(TAG, "FilesTabFragment not found");
                Toast.makeText(getContext(), "Files tab not ready, please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error uploading file", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
