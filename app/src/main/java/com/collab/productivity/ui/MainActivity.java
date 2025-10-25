package com.collab.productivity.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.ui.fragments.HomeFragment;
import com.collab.productivity.ui.fragments.GroupCreationFragment;
import com.collab.productivity.ui.fragments.CollaborationFragment;
import com.collab.productivity.ui.fragments.SettingsFragment;
import com.collab.productivity.viewmodel.GroupViewModel;
import com.collab.productivity.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GroupViewModel groupViewModel;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");
        try {
            // Apply theme before setting content view
            themeManager = new ThemeManager(this);
            themeManager.applyTheme();

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initialize ViewModel
            groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

            // Setup bottom navigation
            setupBottomNavigation();

            // Set default fragment if this is the first creation
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment());
            }

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
        }
        Log.d(TAG, "onCreate - End");
    }

    private void setupBottomNavigation() {
        try {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_group_creation) {
                    selectedFragment = new GroupCreationFragment();
                } else if (itemId == R.id.navigation_collaboration) {
                    selectedFragment = new CollaborationFragment();
                } else if (itemId == R.id.navigation_settings) {
                    selectedFragment = new SettingsFragment();
                }

                return loadFragment(selectedFragment);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            try {
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error loading fragment: " + fragment.getClass().getSimpleName(), e);
            }
        }
        return false;
    }
}
