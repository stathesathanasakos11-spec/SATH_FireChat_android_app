package com.example.p22005unipifirechat;

import android.app.Application;
import com.example.p22005unipifirechat.utils.ThemeManager;

// custom class that extends Application so that I can apply theme and other changes globally
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        //Apply the saved theme preference globally when the app process starts
        // this is called in the onCreate method of the application class
        // so that the theme is applied when the app is launched
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme();
    }
}
