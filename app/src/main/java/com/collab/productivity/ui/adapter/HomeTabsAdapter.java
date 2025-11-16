package com.collab.productivity.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.collab.productivity.ui.fragments.FilesTabFragment;
import com.collab.productivity.ui.fragments.NotesTabFragment;

/**
 * ViewPager adapter for managing Notes and Files tabs
 */
public class HomeTabsAdapter extends FragmentStateAdapter {

    public static final int TAB_NOTES = 0;
    public static final int TAB_FILES = 1;
    public static final int TAB_COUNT = 2;

    private NotesTabFragment notesTabFragment;
    private FilesTabFragment filesTabFragment;

    public HomeTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_NOTES:
                if (notesTabFragment == null) {
                    notesTabFragment = new NotesTabFragment();
                }
                return notesTabFragment;
            case TAB_FILES:
                if (filesTabFragment == null) {
                    filesTabFragment = new FilesTabFragment();
                }
                return filesTabFragment;
            default:
                throw new IllegalArgumentException("Invalid tab position: " + position);
        }
    }

    public NotesTabFragment getNotesTabFragment() {
        return notesTabFragment;
    }

    public FilesTabFragment getFilesTabFragment() {
        return filesTabFragment;
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }

    /**
     * Get tab title for position
     */
    public String getTabTitle(int position) {
        switch (position) {
            case TAB_NOTES:
                return "Notes";
            case TAB_FILES:
                return "Files";
            default:
                return "";
        }
    }
}
