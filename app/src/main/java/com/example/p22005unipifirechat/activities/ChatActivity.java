package com.example.p22005unipifirechat.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.interfaces.SmartReplyListener;
import com.example.p22005unipifirechat.interfaces.IMessageActionListener;
import com.example.p22005unipifirechat.interfaces.MessagesListener;
import com.example.p22005unipifirechat.interfaces.MessageActionResultListener;
import com.example.p22005unipifirechat.interfaces.UserImageListener;
import com.example.p22005unipifirechat.adapters.MessageAdapter;
import com.example.p22005unipifirechat.modelclasses.Message;
import com.example.p22005unipifirechat.utils.AuthManager;
import com.example.p22005unipifirechat.utils.ChatManager;
import com.example.p22005unipifirechat.utils.SmartReplyManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ChatActivity extends BaseActivity {
    private ImageButton btnBack, btnSend, btnSmartReply;
    private HorizontalScrollView scrollSmartReplies;
    private LinearLayout layoutSmartReplies;
    private EditText etMessage;
    private TextView tvChatUser;
    private RecyclerView recyclerChat;
    private View navigationBarSpacer;
    private MessageAdapter messageAdapter;
    private List<Message> mChat;
    private ChatManager chatManager;
    private AuthManager authManager;
    private SmartReplyManager smartReplyManager;
    private SmartReplyListener smartReplyListener;
    private ValueEventListener messagesListener;
    private String otherUserId;
    private String otherUsername;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initManagers();
        initIntentData();
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupWindowInsets();
        loadChatContent();
    }

    private void initManagers() {
        chatManager = ChatManager.getInstance();
        authManager = AuthManager.getInstance();
        currentUserId = authManager.getCurrentUser().getUid();
        smartReplyManager = SmartReplyManager.getInstance();
    }

    private void initIntentData() {
        //από τη MainActivity κατά τη μετακίνηση στην ChatActivity περνάω αυτές τις 2 τιμές
        // με την intent.putExtra() για να τις χρησιμοποιήσω εδώ και να μην ψάχνω το uid του συνομιλητή εδώ
        otherUserId = getIntent().getStringExtra("other_uid");
        otherUsername = getIntent().getStringExtra("other_username");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        tvChatUser = findViewById(R.id.tvChatUser);
        recyclerChat = findViewById(R.id.recyclerChat);
        navigationBarSpacer = findViewById(R.id.navigationBarSpacer);
        tvChatUser.setText(otherUsername);
        btnSmartReply = findViewById(R.id.btnSmartReply);
        scrollSmartReplies = findViewById(R.id.scrollSmartReplies);
        layoutSmartReplies = findViewById(R.id.layoutSmartReplies);
    }

    private void setupRecyclerView() {
        recyclerChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //bottom-up ακολουθία
        linearLayoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(linearLayoutManager);
        mChat = new ArrayList<>();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> handleSendMessage());
        btnSmartReply.setOnClickListener(v -> handleSmartReply());
    }





    private void handleSendMessage() {
        String msg = etMessage.getText().toString().trim();
        // αν το μήνυμα από το πλαίσιο δεν είναι κενό τότε ο ChatManager αναλαμβάνει την επικοινωνία-ενημέρωση της realtime database
        if (!TextUtils.isEmpty(msg)) {
            chatManager.sendMessage(currentUserId, otherUserId, msg, null);
            etMessage.setText("");
        } else {
            Toast.makeText(this, R.string.cannot_send_an_empty_message, Toast.LENGTH_SHORT).show();
        }
    }




    //δίνω στον χρήστη τα chips με τις 3 απαντήσεις
    private void handleSmartReply() {
        smartReplyManager.generateReplies(mChat, currentUserId, new SmartReplyListener() {
            @Override
            public void onRepliesGenerated(List<String> suggestions) {
                //μπορεί να υπήρχαν απαντήσεις από πριν και τις καθαρίζω
                layoutSmartReplies.removeAllViews();

                //αν το AI δεν ξέρει τι να πει
                if (suggestions.isEmpty()) {
                    Toast.makeText(ChatActivity.this, R.string.smart_reply_no_answer, Toast.LENGTH_SHORT).show();
                    return;
                }

                // εμφάνιση της μπάρας με τις smart replies
                scrollSmartReplies.setVisibility(View.VISIBLE);

                //ένα chip για κάθε απάντηση
                for (String reply : suggestions) {
                    Chip chip = new Chip(ChatActivity.this);
                    chip.setText(reply);
                    chip.setCheckable(false);

                    // Προσθήκη margin δεξιά από κάθε Chip για να μην κολλάνε μεταξύ τους
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 16, 0);
                    chip.setLayoutParams(params);

                    // αφού ο χρήστης επιλέξει απάντηση
                    chip.setOnClickListener(v -> {
                        etMessage.setText(reply);
                        scrollSmartReplies.setVisibility(View.GONE);
                    });

                    // 6. Προσθέτουμε το έτοιμο Chip μέσα στο οριζόντιο Layout
                    layoutSmartReplies.addView(chip);
                }
            }

            @Override
            public void onError(String error) {
                String errorMessage = getString(R.string.smart_reply_error) + " " + error;
                Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void loadChatContent() {
        // ανάκτηση εικόνας του συνομιλητή για να δημιουργήσει το περιεχόμενο ο adapter
        chatManager.getUserImage(otherUserId, new UserImageListener() {
            @Override
            public void onImageRetrieved(String imageUrl) {
                setupMessageListener(imageUrl);
            }

            @Override
            public void onError(String error) {
                setupMessageListener("default");
            }
        });
    }

    private void setupMessageListener(String otherUserImageUrl) {
        // αρχικοποιώ έναν listener που θα βρίσκει τις αλλαγές στη ΒΔ μέσω του ChatManager
        messagesListener = chatManager.listenForMessages(currentUserId, otherUserId, new MessagesListener() {
            @Override
            public void onMessagesRetrieved(List<Message> messages) {
                //αν υπάρχει νέο μήνυμα ενημέρωση του UI
                updateUIWithMessages(messages, otherUserImageUrl);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithMessages(List<Message> messages, String imageUrl) {
        mChat.clear();
        mChat.addAll(messages);

        if (messageAdapter == null) {
            //ο messageAdapter επιστρέφει αποτελέσματα μέσω των interfaces
            messageAdapter = new MessageAdapter(ChatActivity.this, mChat, imageUrl, new IMessageActionListener() {
                @Override
                public void onMessageLongClick(Message message) {
                    if (message.senderId.equals(currentUserId)) {
                        showDeleteConfirmationDialog(message);
                    } else {
                        Toast.makeText(ChatActivity.this, R.string.not_allowed_to_delete_messages, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recyclerChat.setAdapter(messageAdapter);
        } else {
            messageAdapter.notifyDataSetChanged();
        }

        scrollToBottom();
    }


    private void scrollToBottom() {
        // ομαλή κύλιση της οθόνης με την έτοιμη συνάρτηση smoothScrollToPosition() της RecyclerView class
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            //αν υπάρχουν στοιχεία στη λίστα των μηνυμάτων
            recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(messageAdapter.getItemCount() - 1));
        }
    }


    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootView), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            
            // If the keyboard is shown, we use IME insets, otherwise we use the Navigation bar height
            int bottomInset = ime.bottom > 0 ? ime.bottom : systemBars.bottom;
            
            if (navigationBarSpacer != null) {
                navigationBarSpacer.getLayoutParams().height = bottomInset;
                navigationBarSpacer.requestLayout();            }
            return WindowInsetsCompat.CONSUMED;
        });

        recyclerChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                scrollToBottom();
            }
        });
    }





    private void deleteMessage(Message message) {
        if (message.getKey() != null) {
            chatManager.deleteMessage(message.getKey(), new MessageActionResultListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ChatActivity.this, R.string.message_deleted_successfully, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDeleteConfirmationDialog(Message message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_message_title)
                .setMessage(R.string.delete_message_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteMessage(message))
                .setNegativeButton(R.string.no, null)
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            FirebaseDatabase.getInstance().getReference("Chats").child(currentUserId).removeEventListener(messagesListener);
        }

        if (smartReplyManager != null) {
            smartReplyManager.close();
        }
    }
}
