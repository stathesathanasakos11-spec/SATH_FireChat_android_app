package com.example.p22005unipifirechat.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.interfaces.AuthListener;
import com.example.p22005unipifirechat.interfaces.ProfileUpdateListener;
import com.example.p22005unipifirechat.interfaces.UserProfileListener;
import com.example.p22005unipifirechat.modelclasses.User;
import com.example.p22005unipifirechat.utils.AuthManager;
import com.example.p22005unipifirechat.utils.AvatarUtils;
import com.example.p22005unipifirechat.utils.LanguageManager;
import com.example.p22005unipifirechat.utils.ProfileManager;
import com.example.p22005unipifirechat.utils.ThemeManager;


public class ProfileActivity extends BaseActivity {
    private TextView tvEmail;
    private EditText etUsername;
    private Button btnSave, btnDeleteAccount, btnChangeLanguage, btnChangeTheme;
    private ImageView imgProfile;
    private ImageButton btnBack;

    private String selectedAvatarId = "default";
    private ProfileManager profileManager;
    private AuthManager authManager;
    private LanguageManager languageManager;
    private ThemeManager themeManager;
    private String currentUserId;

    //to save alertDialogs state during-after screen rotation
    private static final String KEY_OPEN_DIALOG = "key_open_dialog";
    private static final int DIALOG_NONE = 0;
    private static final int DIALOG_DELETE_ACCOUNT = 1;
    private static final int DIALOG_LANGUAGE = 2;
    private static final int DIALOG_THEME = 3;
    private int currentlyOpenDialog = DIALOG_NONE; // active dialog's code

    private AlertDialog activeDialog;   // reference to the currently active dialog



    @Override
    protected void attachBaseContext(Context newBase) {
        languageManager = new LanguageManager(newBase);
        super.attachBaseContext(languageManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupEdgeToEdge();
        initManagers();
        initViews();
        setupClickListeners();
        loadProfileData();

        if (savedInstanceState != null) {
            //reset the currently open dialog

            restoreDialogState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //find which dialog is open during screen's rotation and save its state
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_OPEN_DIALOG, currentlyOpenDialog);
    }

    private void setupEdgeToEdge() {
        //every Activity's content will be edge-to-edge
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void initManagers() {
        //access to util classes that are used in this activity
        profileManager = ProfileManager.getInstance();
        authManager = AuthManager.getInstance();
        themeManager = new ThemeManager(this);
        if (authManager.getCurrentUser() != null) {
            currentUserId = authManager.getCurrentUser().getUid();
        } else {
            finish();
        }
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tvEmail);
        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSave);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        btnChangeTheme = findViewById(R.id.btnChangeTheme);
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        imgProfile.setOnClickListener(v -> showAvatarDialog());
        btnSave.setOnClickListener(v -> handleUpdateProfile());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
        btnChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
        btnChangeTheme.setOnClickListener(v -> showThemeSelectionDialog());
    }



    private void restoreDialogState(Bundle savedInstanceState) {
        //helper method to restore the currently open dialog
        int openDialog = savedInstanceState.getInt(KEY_OPEN_DIALOG, DIALOG_NONE);

        if (openDialog == DIALOG_DELETE_ACCOUNT) {
            showDeleteAccountConfirmation();
        } else if (openDialog == DIALOG_LANGUAGE) {
            showLanguageSelectionDialog();
        } else if (openDialog == DIALOG_THEME) {
            showThemeSelectionDialog();
        }
    }




    // only ProfileManager can load the user's profile from the database. NOT this Activity
    private void loadProfileData() {
        profileManager.loadUserProfile(currentUserId, new UserProfileListener() {
            @Override
            public void onProfileLoaded(User user) {
                // set values in the proper UI fields
                tvEmail.setText(user.email);
                etUsername.setText(user.username);
                if (user.imageUrl != null) {
                    selectedAvatarId = user.imageUrl;
                    AvatarUtils.setAvatar(imgProfile, selectedAvatarId);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateProfile() {
        String newUsername = etUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            etUsername.setError(getString(R.string.username_required));
            return;
        }

        //after profileManager handles the updates in the database
        // app returns to the Activity that called it
        profileManager.updateProfile(currentUserId, newUsername, selectedAvatarId, new ProfileUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, R.string.your_profile_updated, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }






    // user's account deletion from Firebase
    private void showDeleteAccountConfirmation() {
        currentlyOpenDialog = DIALOG_DELETE_ACCOUNT;

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> handleDeleteAccount())
                .setNegativeButton(R.string.no, null)
                .create();

        activeDialog.setOnDismissListener(dialog -> {
            currentlyOpenDialog = DIALOG_NONE;
            activeDialog = null;
        });

        activeDialog.show();
    }

    private void handleDeleteAccount() {
        // AuthManager is used to delete the user's account from Firebase Authentication
        authManager.deleteUserAccount(new AuthListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, R.string.account_deleted_successfully, Toast.LENGTH_SHORT).show();
                // navigate to the login screen again
                navigateToLogin();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ProfileActivity.this, getString(R.string.failed_to_delete_account) + ": " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }





    // user language selection
    private void showLanguageSelectionDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.greek)};
        String[] languageCodes = {"en", "el"};

        currentlyOpenDialog = DIALOG_LANGUAGE;

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setItems(languages, (dialog, which) -> {
                    String selectedCode = languageCodes[which];

                    currentlyOpenDialog = DIALOG_NONE;
                    activeDialog = null;

                    // languageManager has access to shared preferences
                    languageManager.updateResource(this, selectedCode);

                    // I recreate this activity and load the proper strings resources
                    recreate();

                    /*
                    // navigate to MainActivity as string resources have been updated
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    */

                })
                .create();

        activeDialog.setOnDismissListener(dialog -> {
            currentlyOpenDialog = DIALOG_NONE;
            activeDialog = null;
        });

        activeDialog.show();
    }




    // theme selection
    private void showThemeSelectionDialog() {
        //use string resources for the theme names instead of hardcoded strings
        String[] themes = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_system)
        };

        currentlyOpenDialog = DIALOG_THEME;

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.select_theme)
                .setItems(themes, (dialog, which) -> {
                    // user completed the theme selection
                    // reset the currently open dialog so that not another dialog is opened while recreating teh Activity
                    currentlyOpenDialog = DIALOG_NONE;
                    activeDialog = null;

                    // only ThemeManager has the right to change the app's theme
                    themeManager.applyTheme(which); // 0: Light, 1: Dark, 2: System
                })
                .create();

        activeDialog.setOnDismissListener(dialog -> {
            currentlyOpenDialog = DIALOG_NONE;
            activeDialog = null;
        });

        activeDialog.show();
    }





    // select an avatar
    private void showAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        // use a grid to display the avatars. User can select one of them
        GridLayout grid = dialogView.findViewById(R.id.gridLayoutAvatars);

        // avatars placed in res-drawable
        int[] avatarDrawables = {
                R.drawable.man1, R.drawable.man2, R.drawable.man3,
                R.drawable.simpleuser, R.drawable.woman1, R.drawable.woman2
        };

        String[] avatarNames = {
                "man1", "man2", "man3", "simpleuser", "woman1", "woman2"
        };

        // avatars placed one by one in the grid
        for (int i = 0; i < avatarDrawables.length; i++) {
            final int resId = avatarDrawables[i];
            final String avatarIdName = avatarNames[i];

            ImageView iv = new ImageView(this);
            iv.setImageResource(resId);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 180;
            params.height = 180;
            params.setMargins(16, 16, 16, 16);
            iv.setLayoutParams(params);

            iv.setOnClickListener(v -> {
                selectedAvatarId = avatarIdName;
                //avatarUtils is used to set the avatar in the UI
                AvatarUtils.setAvatar(imgProfile, selectedAvatarId);
                dialog.dismiss();
            });

            grid.addView(iv);
        }
        dialog.show();
    }




    @Override
    protected void onDestroy() {
        //close the active dialog if it is still open
        // because the activity is now destroyed
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
        activeDialog = null;
        super.onDestroy();
    }
}
