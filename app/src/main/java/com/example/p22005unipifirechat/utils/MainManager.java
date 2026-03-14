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
    // επικοινωνία με τη Realtime Database για διαχωρισμό επιπέδων με την MainActivity
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
        // κόμβος ChatList στον χρήστη με το UID του
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

        // άμεση ενημέρωση της MainActivity γισ την κατάστασης ενός κόμβου του chatList node
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
                        //ενημέρωση timestamp τελευταίου μηνύματος
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




    //αναζήτηση χρήστη στη realtime databse με το ακριβές username από την MainActivity
    public void findUserByUsername(String username, UserSearchListener listener) {
        // όλοι οι χρήστες σε σειρά και αναζήτηση με συγκεκριμένου username
        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //αν βρεθεί το username κρατάω το UID του χρήστη που εμφανίστηκε πρώτη φορά
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                String foundUsername = userSnapshot.child("username").getValue(String.class);
                                listener.onUserFound(uid, foundUsername);
                                return; // stop loop after first successful match
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
        //αφαίρεση συνομιλίας από τη βάση για τον currentUser δοθέντων των UID των εμπλεκόμενων χρηστών
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
