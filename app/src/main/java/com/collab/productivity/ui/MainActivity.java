package com.collab.productivity.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.ui.fragments.HomeFragment;
import com.collab.productivity.ui.fragments.GroupCreationFragment;
import com.collab.productivity.ui.fragments.CollaborationFragment;
import com.collab.productivity.ui.fragments.SettingsFragment;
import com.collab.productivity.viewmodel.GroupViewModel;
import com.collab.productivity.utils.ThemeManager;
import com.collab.productivity.utils.Logger;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GroupViewModel groupViewModel;
    private ThemeManager themeManager;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme manager and apply theme before super.onCreate
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();

        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate - Start");

        try {
            setContentView(R.layout.activity_main);

            // Initialize FragmentManager
            fragmentManager = getSupportFragmentManager();

            // Initialize ViewModel
            groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

            // Setup bottom navigation
            setupBottomNavigation();

            // Set default fragment if this is the first creation
            if (savedInstanceState == null) {
                Fragment homeFragment = new HomeFragment();
                currentFragment = homeFragment;
                loadFragment(homeFragment);
            }

        } catch (Exception e) {
            Logger.e(TAG, "Critical error in onCreate", e);
        }
        Logger.d(TAG, "onCreate - End");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check and apply any theme changes that might have occurred
        themeManager.applyTheme();
    }

    private void setupBottomNavigation() {
        try {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            if (navView == null) {
                Log.e(TAG, "Bottom navigation view not found");
                return;
            }

            navView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                String fragmentTag = "";
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                    fragmentTag = "home";
                } else if (itemId == R.id.navigation_group_creation) {
                    selectedFragment = new GroupCreationFragment();
                    fragmentTag = "group_creation";
                } else if (itemId == R.id.navigation_collaboration) {
                    selectedFragment = new CollaborationFragment();
                    fragmentTag = "collaboration";
                } else if (itemId == R.id.navigation_settings) {
                    selectedFragment = new SettingsFragment();
                    fragmentTag = "settings";
                }

                if (selectedFragment != null) {
                    Log.d(TAG, "Loading fragment: " + fragmentTag);
                    return loadFragment(selectedFragment);
                }
                return false;
            });

            // Set initial selection
            navView.setSelectedItemId(R.id.navigation_home);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            try {
                Log.d(TAG, "Starting fragment transaction");

                // Don't reload the same fragment
                if (currentFragment != null &&
                    currentFragment.getClass().equals(fragment.getClass())) {
                    Log.d(TAG, "Fragment already active, skipping transaction");
                    return true;
                }

                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out)
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();

                fragmentManager.executePendingTransactions();
                currentFragment = fragment;
                Log.d(TAG, "Fragment transaction completed successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error loading fragment: " + fragment.getClass().getSimpleName(), e);
            }
        } else {
            Log.e(TAG, "Attempted to load null fragment");
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
