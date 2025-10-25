package com.collab.productivity.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.collab.productivity.R;
import com.collab.productivity.utils.ThemeManager;
import com.collab.productivity.utils.Logger;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private ThemeManager themeManager;
    private SwitchMaterial themeSwitch;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeManager = new ThemeManager(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        themeSwitch = view.findViewById(R.id.theme_switch);
        setupThemeSwitch();

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
}
