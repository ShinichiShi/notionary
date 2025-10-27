package com.collab.productivity.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.ui.fragments.HomeFragment;
import com.collab.productivity.ui.fragments.GroupCreationFragment;
import com.collab.productivity.ui.fragments.CollaborationFragment;
import com.collab.productivity.ui.fragments.SettingsFragment;
import com.collab.productivity.utils.ThemeManager;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.viewmodel.GroupViewModel;
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

            // Handle deep links
            handleDeepLink(getIntent());

        } catch (Exception e) {
            Logger.e(TAG, "Critical error in onCreate", e);
        }
        Logger.d(TAG, "onCreate - End");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                Logger.d(TAG, "Deep link received: " + data.toString());

                // Check if it's a join group link
                if (data.getPath() != null && data.getPath().contains("/join")) {
                    String inviteCode = data.getQueryParameter("code");
                    if (inviteCode != null && !inviteCode.isEmpty()) {
                        Logger.d(TAG, "Invite code from deep link: " + inviteCode);
                        joinGroupFromDeepLink(inviteCode);
                    }
                }
            }
        }
    }

    private void joinGroupFromDeepLink(String inviteCode) {
        // Show collaboration fragment
        BottomNavigationView navView = findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setSelectedItemId(R.id.navigation_collaboration);
        }

        // Join the group
        groupViewModel.joinGroup(inviteCode, new GroupViewModel.GroupCreationCallback() {
            @Override
            public void onSuccess(com.collab.productivity.data.model.Group group) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                        "Successfully joined: " + group.getName(),
                        Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                        "Error joining group: " + error,
                        Toast.LENGTH_LONG).show();
                });
            }
        });
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
