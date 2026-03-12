package com.example.p22005unipifirechat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.interfaces.AuthListener;
import com.example.p22005unipifirechat.utils.AuthManager;
import com.example.p22005unipifirechat.utils.LanguageManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends BaseActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button authButton;
    private TextView tvToggle, tvTitle;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    //φτιάχνω ένα AuthManager object για το Firebase Authentication εκεί και να κρατήσω την Activity απλή
    private AuthManager authManager;
    private boolean isSignUp = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        // ορισμός γλώσσας και θέματος ξανά εφόσον ο χρήστης άλλαξε τις προτιμήσεις του στο profile screen του
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupEdgeToEdge();

        authManager = AuthManager.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        authButton = findViewById(R.id.authButton);
        tvToggle = findViewById(R.id.tvToggle);
        tvTitle = findViewById(R.id.tvTitle);
        progressBar = findViewById(R.id.progressBar);

        // vibration για το login και το register
        authButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
            }
            return false;
        });

        // παίρνω τα στοιχεία και το μοναδικό Id του google χρήστη για το sing-in/sing-up
        // το web_cilent_id είναι το token σύνδεσης του project με το project στη Firebase
        // για να ξέρει έτσι η εφαρμογή και το Google με ποια ΒΔ συνομιλεί
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btnGoogle = findViewById(R.id.btnGoogleSign);
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        authButton.setOnClickListener(v -> handleAuthAction());

        tvToggle.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            toggleMode();
        });
    }

    private void setupEdgeToEdge() {
        View root = findViewById(R.id.loginRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // αν υπάρχει ο χρήστης και δεν είναι null τότε πάμε στο Home page - MainActivity
        if (authManager.getCurrentUser() != null) {
            goToMainActivity();
        }
    }




    // ποια φόρμα θα εμφανιστεί (login ή sign-up)
    private void toggleMode() {
        isSignUp = !isSignUp;
        View tilUsername = findViewById(R.id.tilUsername);
        if (isSignUp) {
            if (tilUsername != null) tilUsername.setVisibility(View.VISIBLE);
            authButton.setText(R.string.sign_up);
            tvToggle.setText(R.string.already_have_account_login);
            tvTitle.setText(R.string.welcome_to_app);
        } else {
            if (tilUsername != null) tilUsername.setVisibility(View.GONE);
            authButton.setText(R.string.log_in);
            tvToggle.setText(R.string.dont_have_account_signup);
            tvTitle.setText(R.string.welcome_back);
        }
    }





    private void handleAuthAction() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        // user's input validation check
        if (!validateUsersInput(email, password, username)) return;
        setLoading(true);

        //κλήση του AuthManager για την επικοινωνία με τη Firebase και από εκεί καθορίζεται τι γίνεται για επιτυχία/αποτυχία
        AuthListener listener = new AuthListener() {
            @Override
            public void onSuccess() {
                setLoading(false);
                goToMainActivity();
            }

            @Override
            public void onFailure(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        // καλώ τις μεθόδους της Firebase για login ή register με τα απαραίτητα credentials
        if (isSignUp) {
            authManager.signUp(email, password, username, listener);
        } else {
            authManager.signIn(email, password, listener);
        }
    }

    private boolean validateUsersInput(String email, String password, String username){
        //χρησιμοποιώ την isEmpty() της TextUtils για τον έλεγχο κειμένου
        // η Petterns library του android έχει τα regular expressions και ελέγχω με αυτά το input του χρήστη
        // πχ αν για τον EMAIL_ADDRESS το input είναι x...@y.z τότε θα επιστρέψει TRUE
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.valid_email_required));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.strong_password_required));
            return false;
        }
        if (isSignUp && TextUtils.isEmpty(username)) {
            etUsername.setError(getString(R.string.username_required));
            return false;
        }
        return true;
    }






    private void signInWithGoogle() {
        setLoading(true);
        // ανοίγω το Intent της Google για περιπτώσεις sign-in
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // μέθοδος για το Google authentication
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // αν η google δώσει το OK τότε δημιουργείται ένα token
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                setLoading(false);
                Toast.makeText(this, getString(R.string.google_sign_in_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        // στέλνω το token του χρήστη στη Firebase για να προχωρήσει η διαδικασία & επικοινωνία με τη Firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authManager.firebaseAuthWithGoogle(credential, new AuthListener() {
            @Override
            public void onSuccess() {
                setLoading(false);
                goToMainActivity();
            }

            @Override
            public void onFailure(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }






    private void setLoading(boolean isLoading) {
        // για να μην πατηθεί το κουμπί πολλές φορές κάνω ένα εφέ φόρτωσης
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        authButton.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
