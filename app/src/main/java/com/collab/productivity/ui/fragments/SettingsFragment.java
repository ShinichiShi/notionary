package com.collab.productivity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.collab.productivity.R;
import com.collab.productivity.ui.LoginActivity;
import com.collab.productivity.utils.FirebaseManager;
import com.collab.productivity.utils.ThemeManager;
import com.collab.productivity.utils.Logger;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private ThemeManager themeManager;
    private FirebaseManager firebaseManager;
    private SwitchMaterial themeSwitch;
    private TextView userEmailText;
    private TextView userNameText;
    private Button logoutButton;
    private Button syncButton;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeManager = new ThemeManager(requireContext());
        firebaseManager = FirebaseManager.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        themeSwitch = view.findViewById(R.id.theme_switch);
        userEmailText = view.findViewById(R.id.user_email);
        userNameText = view.findViewById(R.id.user_name);
        logoutButton = view.findViewById(R.id.logout_button);
        syncButton = view.findViewById(R.id.sync_button);

        setupThemeSwitch();
        setupUserInfo();
        setupLogoutButton();
        setupSyncButton();

        return view;
    }

    private void setupThemeSwitch() {
        // Set initial state
        boolean isDarkMode = themeManager.getTheme() == AppCompatDelegate.MODE_NIGHT_YES;
        themeSwitch.setChecked(isDarkMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Logger.d(TAG, "Theme switch changed to: " + isChecked);
            themeManager.setTheme(isChecked ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void setupUserInfo() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user != null) {
            if (userEmailText != null) {
                userEmailText.setText(user.getEmail());
            }
            if (userNameText != null) {
                String displayName = user.getDisplayName();
                userNameText.setText(displayName != null ? displayName : "User");
            }
        }
    }

    private void setupLogoutButton() {
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        }
    }

    private void setupSyncButton() {
        if (syncButton != null) {
            syncButton.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Syncing data...", Toast.LENGTH_SHORT).show();
                // Trigger sync in ViewModels if needed
            });
        }
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                firebaseManager.signOut();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
