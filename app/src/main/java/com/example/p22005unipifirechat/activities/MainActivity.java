package com.example.p22005unipifirechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.interfaces.IUsersActionListener;
import com.example.p22005unipifirechat.interfaces.ChatListListener;
import com.example.p22005unipifirechat.interfaces.ActionResultListener;
import com.example.p22005unipifirechat.interfaces.UserSearchListener;
import com.example.p22005unipifirechat.adapters.UsersAdapter;
import com.example.p22005unipifirechat.modelclasses.User;
import com.example.p22005unipifirechat.utils.AuthManager;
import com.example.p22005unipifirechat.utils.MainManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen displaying chat list.
 * Extends BaseActivity for unified theme and language handling.
 */
public class MainActivity extends BaseActivity {

    private RecyclerView recyclerViewChats;
    private FloatingActionButton fabNewChat;
    private TextView tvEmptyState;
    private ImageButton btnLogout;

    private UsersAdapter usersAdapter;
    private List<User> userList;

    private AuthManager authManager;
    private MainManager mainManager;
    private String currentUserId;
    private ValueEventListener chatListListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupEdgeToEdge();

        initManagers();
        if (authManager.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }
        currentUserId = authManager.getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        observeChats();
    }

    private void setupEdgeToEdge() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }
        
        View fab = findViewById(R.id.fabNewChat);
        if (fab != null) {
            ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    float density = getResources().getDisplayMetrics().density;
                    params.bottomMargin = (int) (24 * density) + systemBars.bottom;
                    params.setMarginEnd((int) (24 * density) + systemBars.right);
                    v.setLayoutParams(params);
                }
                return insets;
            });
        }
    }

    private void initManagers() {
        authManager = AuthManager.getInstance();
        mainManager = MainManager.getInstance();
    }

    private void initViews() {
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        fabNewChat = findViewById(R.id.fabNewChat);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupRecyclerView() {
        recyclerViewChats.setHasFixedSize(true);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, userList, new IUsersActionListener() {
            @Override
            public void onUserClicked(User user) {
                openChatActivity(user.uid, user.username);
            }

            @Override
            public void onDeleteChatClicked(User user) {
                showDeleteChatDialog(user);
            }
        });
        recyclerViewChats.setAdapter(usersAdapter);
    }

    private void setupClickListeners() {
        fabNewChat.setOnClickListener(v -> showNewChatDialog());
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void observeChats() {
        chatListListener = mainManager.observeChatList(currentUserId, new ChatListListener() {
            @Override
            public void onChatListUpdated(List<User> users) {
                tvEmptyState.setVisibility(View.GONE);
                recyclerViewChats.setVisibility(View.VISIBLE);
                userList.clear();
                userList.addAll(users);
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onEmpty() {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerViewChats.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> handleLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void handleLogout() {
        authManager.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> navigateToLogin());
    }

    private void showDeleteChatDialog(User user) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.delete_chat_title)
                .setMessage(getString(R.string.delete_chat_confirmation, user.username))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mainManager.deleteChat(currentUserId, user.uid, new ActionResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, R.string.chat_deleted_successfully, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showNewChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_chat);
        builder.setMessage(R.string.enter_username_for_new_chat);

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(R.string.chat, (dialog, which) -> {
            String usernameInput = input.getText().toString().trim();
            if (!usernameInput.isEmpty()) {
                mainManager.findUserByUsername(usernameInput, new UserSearchListener() {
                    @Override
                    public void onUserFound(String uid, String username) {
                        openChatActivity(uid, username);
                    }

                    @Override
                    public void onUserNotFound() {
                        Toast.makeText(MainActivity.this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openChatActivity(String uid, String username) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("other_uid", uid);
        intent.putExtra("other_username", username);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListListener != null) {
            FirebaseDatabase.getInstance().getReference("ChatList").child(currentUserId).removeEventListener(chatListListener);
        }
    }
}
