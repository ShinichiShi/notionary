package com.collab.productivity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager - Manages app theme preferences
 * Handles light/dark mode toggle and persistence
 */
public class ThemeManager {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private SharedPreferences sharedPreferences;
    private Context context;

    /**
     * Constructor
     * @param context Application context
     */
    public ThemeManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if dark mode is enabled
     * @return true if dark mode is enabled, false otherwise
     */
    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Sets dark mode preference
     * @param enabled true to enable dark mode, false to disable
     */
    public void setDarkMode(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    /**
     * Applies the saved theme to the app
     * Should be called before setContentView() in activities
     */
    public void applyTheme() {
        if (isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
