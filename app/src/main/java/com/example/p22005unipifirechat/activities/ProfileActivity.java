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
    }

    private void setupEdgeToEdge() {
        //για κάθε Activity το περιεχόμενο δεν πρέπει να κρύβεται πίσω από τα status bars
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






    // παίρνω τασ δεδομένα από τον ProfileManager γιατί εκεί επικοινωνεί με τη ΒΔ
    private void loadProfileData() {
        profileManager.loadUserProfile(currentUserId, new UserProfileListener() {
            @Override
            public void onProfileLoaded(User user) {
                // βάζω τα δεδομένα που ανάκτησα στα views πεδία
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

        // κάνει ο ProfileManager την ενημέρωση της realtime database
        // επιστρέφει η εφαρμογή εδώ για να εμφανίζει το κατάλληλο μήνυμα
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






    // διαγραφή προφίλ χρήστη από την εφαρμογή
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> handleDeleteAccount())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void handleDeleteAccount() {
        // ανακατεύθυνση στον AuthManager για τη διαγραφή του χρήστη από το Firebase Authentication
        // μόνο ο AuthManager έχει πρόσβαση στη ΒΔ
        authManager.deleteUserAccount(new AuthListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, R.string.account_deleted_successfully, Toast.LENGTH_SHORT).show();
                // και πάει τον χρήστη στο auth screen ξανά
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





    // επιλογή γλώσσας εφαρμογής
    private void showLanguageSelectionDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.greek)};
        String[] languageCodes = {"en", "el"};

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setItems(languages, (dialog, which) -> {
                    String selectedCode = languageCodes[which];
                    // μόνο η LnaguageManager μπορεί να επέμβει στις ρυθμίσεις των shared preferences απευθείας
                    languageManager.updateResource(this, selectedCode);
                    // ανακατεύθυνση στη MainActivity (home screen) για να εμφανιστεί η νέα γλώσσα
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }




    // αλλαγή θέματος εφαρμογής
    private void showThemeSelectionDialog() {
        // από τα string resources για να υποστηρίζονται πολλές γλώσσες
        String[] themes = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_system)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_theme)
                .setItems(themes, (dialog, which) -> {
                    // μόνο η ThemeManager έχει απευθείας πρόσβαση στην αλλαγή θέματος εφαρμογής
                    themeManager.applyTheme(which); // 0: Light, 1: Dark, 2: System
                })
                .show();
    }





    // select an avatar
    private void showAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_avatar, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        // για επιλογή στοιχείων από το grid που φτιάχνω παρακάτω
        GridLayout grid = dialogView.findViewById(R.id.gridLayoutAvatars);

        // τα εικονίδια από τo res/drawable
        int[] avatarDrawables = {
                R.drawable.man1, R.drawable.man2, R.drawable.man3,
                R.drawable.simpleuser, R.drawable.woman1, R.drawable.woman2
        };

        String[] avatarNames = {
                "man1", "man2", "man3", "simpleuser", "woman1", "woman2"
        };

        // ένα grid με τα εικονίδια στη σειρά για να επιλέξει ο χρήστης
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
                //λέω AvatarUtils να εμφανίσει το εικονίδιο που επιλέγει ο χρήστης
                AvatarUtils.setAvatar(imgProfile, selectedAvatarId);
                dialog.dismiss();
            });

            grid.addView(iv);
        }
        dialog.show();
    }
}
