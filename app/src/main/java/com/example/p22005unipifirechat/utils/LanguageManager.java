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

    //αποθηκεύω την επιλογή του χρήστη όταν κάνει αλλαγή στην Profile Activity
    // και ενημερώνω τα resources της εφαρμογής στη συσκευή
    public void updateResource(Context context, String code) {
        saveLanguage(code);
        updateResources(context, code);
    }


    // ενημερώνω το Locale και την καλώ μέσω της BaseActivity για κάθε activity της οθόνης
    public Context setLocale(Context context) {
        return updateResources(context, getLanguage());
    }



    // καθορίζω ποιο string.xml file θα χρησιμοποιηθεί
    private Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        // από τα res της εφαρμογής
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            // για παλιές εκδόσεις android
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }


    public void saveLanguage(String code) {
        // κάθε γλώσσα έχει κωδικό πχ en, el, fr
        sharedPreferences.edit().putString(KEY_LANG, code).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANG, "en"); // Default to English
    }
}
