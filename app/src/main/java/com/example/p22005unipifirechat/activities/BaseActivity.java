package com.example.p22005unipifirechat.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.p22005unipifirechat.utils.LanguageManager;
import com.example.p22005unipifirechat.utils.ThemeManager;

//all the activities extend "BaseActivity" that extends AppCompatActivity
// αυτή η μέθοδος είναι ο ενδιάμεσος μεταξύ των Activities και πχ του ThemeManager ή LanguageManager
//Αν ο χρήστης αλλάξει θέμα ενώ ήδη τρέχει την εφαρμογή, μέσω της κληρονομικότητας όλα τα
// activities θα λάβουν την ενημέρωση του θέματος & γλώσσας από την BaseActivity
public abstract class BaseActivity extends AppCompatActivity {
    protected LanguageManager languageManager;
    protected ThemeManager themeManager;

    // καλείται από τη setLocale() στην LanguageManager και θέτει την αλλαγή στις προτιμήσεις του χρήστη στο Locale
    @Override
    protected void attachBaseContext(Context newBase) {
        languageManager = new LanguageManager(newBase);
        // Apply the saved (by user) language context
        super.attachBaseContext(languageManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        
        // Ensure the correct theme is applied before super.onCreate() method
        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
    }
}
