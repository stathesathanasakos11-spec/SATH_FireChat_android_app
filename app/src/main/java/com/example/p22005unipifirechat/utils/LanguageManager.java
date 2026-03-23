package com.example.p22005unipifirechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class LanguageManager {
    private static final String PREF_NAME = "language_pref";
    private static final String KEY_LANG = "selected_language";
    private final SharedPreferences sharedPreferences;

    public LanguageManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // after user changes the language by settings (on ProfileActivity) the new language has to be applied immediately
    public void updateResource(Context context, String code) {
        saveLanguage(code);
        updateResources(context, code);
    }


    // I call this method from BaseActivity.java to update string resources before onCreate() method (for each Activity)
    public Context setLocale(Context context) {
        return updateResources(context, getLanguage());
    }



    // select the string .xml file according to the selected language
    private Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        // extract from the resources
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            // for older Android versions
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }


    public void saveLanguage(String code) {
        // each language has its unique code (e.g. en, gr)
        sharedPreferences.edit().putString(KEY_LANG, code).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANG, "en"); // Default to English
    }
}
