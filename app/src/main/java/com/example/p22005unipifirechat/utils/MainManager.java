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


public class MainManager {
    //Singleton Design Pattern
    private static MainManager instance;
    // only MainManager has access to the database
    // this way I separate the UI and the data access layers
    private final DatabaseReference mDatabase;
    //keeps track of the last message time for each user
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
                    //save the last message time for each user in the userLastMsgMap for currentUser
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

        // immediate update of MainActivity's recyclerView for each change in the ChatList
        chatListRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }





    private void fetchUserDetails(String userId, ChatListListener listener) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Remove existing entry to avoid duplicates chats in the recyclerView
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).uid.equals(user.uid)) {
                            userList.remove(i);
                            break;
                        }
                    }

                    if (userLastMsgMap.containsKey(user.uid)) {
                        // update the timestamp of the last one message
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




    //find a user by his username and return his UID
    public void findUserByUsername(String username, UserSearchListener listener) {
        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // use the UID of the first user that matches the username
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                String foundUsername = userSnapshot.child("username").getValue(String.class);
                                listener.onUserFound(uid, foundUsername);
                                return; // stop the loop after first successful match
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




    public void deleteChat(String currentUserId, String otherUserId, ActionResultListener listener) {
        //remove this chat from currentUser's node using ChatList and the UIDs of the two participants of the chat
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
