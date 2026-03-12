package com.example.p22005unipifirechat;

import android.app.Application;
import com.example.p22005unipifirechat.utils.ThemeManager;

// custom class that extends Application so that I can apply theme and other changes globally
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply the saved theme preference globally when the app process starts
        // η κλάση τρέχει μία φορά (singleton) καθε φορά που ξεκινάει η εφαρμογή
        // και τερματίζει μόλις κλείσει η εφαρμογή, οπότε ότι ορίζεται εδώ δημιουργείται ήδη από
        //την έναρξη της εφαρμογής
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme();
    }
}
