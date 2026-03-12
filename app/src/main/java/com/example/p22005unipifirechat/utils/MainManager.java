package com.example.p22005unipifirechat.utils;

import androidx.annotation.NonNull;

import com.example.p22005unipifirechat.modelclasses.User;
import com.example.p22005unipifirechat.interfaces.ChatListListener;
import com.example.p22005unipifirechat.interfaces.UserSearchListener;
import com.example.p22005unipifirechat.interfaces.ActionResultListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager responsible for the business logic of the Main screen.
 * Handles chat list retrieval, user searching, and chat deletion.
 */
public class MainManager {

    private static MainManager instance;
    private final DatabaseReference mDatabase;
    private final Map<String, Long> userLastMsgMap = new HashMap<>();
    private final List<User> userList = new ArrayList<>();

    private MainManager() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized MainManager getInstance() {
        if (instance == null) {
            instance = new MainManager();
        }
        return instance;
    }

    /**
     * Listens for changes in the user's chat list and fetches details for each chat partner.
     */
    public ValueEventListener observeChatList(String currentUserId, ChatListListener listener) {
        DatabaseReference chatListRef = mDatabase.child("ChatList").child(currentUserId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                userLastMsgMap.clear();

                if (!snapshot.exists()) {
                    listener.onEmpty();
                    return;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String otherUserId = dataSnapshot.getKey();
                    long time = 0;
                    if (dataSnapshot.hasChild("date")) {
                        Object dateObj = dataSnapshot.child("date").getValue();
                        if (dateObj instanceof Long) {
                            time = (Long) dateObj;
                        }
                    }
                    userLastMsgMap.put(otherUserId, time);
                    fetchUserDetails(otherUserId, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        chatListRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    private void fetchUserDetails(String userId, ChatListListener listener) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Remove existing entry to avoid duplicates
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).uid.equals(user.uid)) {
                            userList.remove(i);
                            break;
                        }
                    }

                    if (userLastMsgMap.containsKey(user.uid)) {
                        user.lastMsgTime = userLastMsgMap.get(user.uid);
                    }

                    userList.add(user);
                    // Sort by last message time descending
                    Collections.sort(userList, (u1, u2) -> Long.compare(u2.lastMsgTime, u1.lastMsgTime));
                    listener.onChatListUpdated(new ArrayList<>(userList));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Searches for a user by their exact username.
     */
    public void findUserByUsername(String username, UserSearchListener listener) {
        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                String foundUsername = userSnapshot.child("username").getValue(String.class);
                                listener.onUserFound(uid, foundUsername);
                                return; // Return after the first match
                            }
                        } else {
                            listener.onUserNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Deletes a chat from the user's ChatList.
     */
    public void deleteChat(String currentUserId, String otherUserId, ActionResultListener listener) {
        mDatabase.child("ChatList").child(currentUserId).child(otherUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else {
                        if (listener != null) listener.onError(task.getException() != null ? task.getException().getMessage() : "Deletion failed");
                    }
                });
    }
}
