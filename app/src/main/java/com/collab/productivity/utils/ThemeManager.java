package com.collab.productivity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String PREF_THEME = "app_theme";
    private static final String TAG = "ThemeManager";
    private final Context context;
    private final SharedPreferences preferences;

    public ThemeManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Logger.d(TAG, "ThemeManager initialized");
    }

    public void applyTheme() {
        int theme = getTheme();
        Logger.d(TAG, "Applying theme: " + theme);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    public void setTheme(int theme) {
        Logger.d(TAG, "Setting theme to: " + theme);
        preferences.edit().putInt(PREF_THEME, theme).apply();
        applyTheme();
    }

    public int getTheme() {
        return preferences.getInt(PREF_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void toggleTheme() {
        int currentTheme = getTheme();
        int newTheme = (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        Logger.d(TAG, "Toggling theme from " + currentTheme + " to " + newTheme);
        setTheme(newTheme);
    }
}
