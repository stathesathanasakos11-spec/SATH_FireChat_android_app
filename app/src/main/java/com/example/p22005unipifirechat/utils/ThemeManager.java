package com.example.p22005unipifirechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_THEME = "selected_theme";
    // κωδικοί για κάθε theme mode
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    private final SharedPreferences sharedPreferences;

    public ThemeManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public void applyTheme() {
        int theme = getTheme();
        applyTheme(theme);
    }


    public void applyTheme(int theme) {
        // άμεση επιβολή θέματος στα activities μέσω της BaseActivity
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        saveTheme(theme);
    }

    private void saveTheme(int theme) {
        // ενημέρωση του αρχείου των share preferences
        sharedPreferences.edit().putInt(KEY_THEME, theme).apply();
    }

    public int getTheme() {
        return sharedPreferences.getInt(KEY_THEME, THEME_SYSTEM);
    }
}
