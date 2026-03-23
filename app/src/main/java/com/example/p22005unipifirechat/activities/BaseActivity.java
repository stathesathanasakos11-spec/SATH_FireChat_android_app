package com.example.p22005unipifirechat.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.p22005unipifirechat.utils.LanguageManager;
import com.example.p22005unipifirechat.utils.ThemeManager;

//all the activities extend "BaseActivity" that extends AppCompatActivity
// Base class for all activities to centralize common logic like theme and language switching.
// If the user changes the settings while the app is running, the inheritance ensures that all
// activities apply the new configuration globally
public abstract class BaseActivity extends AppCompatActivity {
    //BaseActivity is a middleware between Activities and Managers
    protected LanguageManager languageManager;
    protected ThemeManager themeManager;


    @Override
    protected void attachBaseContext(Context newBase) {
        //LanguageManager.setLocale() calls this method to set the language context
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
