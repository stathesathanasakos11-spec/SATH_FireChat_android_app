package com.example.p22005unipifirechat.utils;

import androidx.annotation.NonNull;

import com.example.p22005unipifirechat.modelclasses.Message;
import com.example.p22005unipifirechat.interfaces.MessagesListener;
import com.example.p22005unipifirechat.interfaces.MessageActionResultListener;
import com.example.p22005unipifirechat.interfaces.UserImageListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatManager {

    private static ChatManager instance;
    private final DatabaseReference mDatabase;

    private ChatManager() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }


    public void sendMessage(String senderId, String receiverId, String messageText, MessageActionResultListener listener) {
        DatabaseReference newMsgRef = mDatabase.child("Chats").push();
        String messageId = newMsgRef.getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderId", senderId);
        hashMap.put("receiverId", receiverId);
        hashMap.put("messageText", messageText);
        hashMap.put("timestamp", System.currentTimeMillis());
        hashMap.put("key", messageId);

        newMsgRef.setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateChatList(senderId, receiverId);
                if (listener != null) listener.onSuccess();
            } else {
                if (listener != null) listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Failed to send message");
            }
        });
    }

    private void updateChatList(String senderId, String receiverId) {
        long currentTime = System.currentTimeMillis();

        // Update ChatList for sender
        DatabaseReference chatRefSender = mDatabase.child("ChatList").child(senderId).child(receiverId);
        chatRefSender.child("id").setValue(receiverId);
        chatRefSender.child("date").setValue(currentTime);

        // Update ChatList for receiver
        DatabaseReference chatRefReceiver = mDatabase.child("ChatList").child(receiverId).child(senderId);
        chatRefReceiver.child("id").setValue(senderId);
        chatRefReceiver.child("date").setValue(currentTime);
    }

    /**
     * Sets up a real-time listener for messages between two users.
     */
    public ValueEventListener listenForMessages(String myId, String otherUserId, MessagesListener listener) {
        DatabaseReference reference = mDatabase.child("Chats");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> mChat = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat != null) {
                        chat.setKey(snapshot.getKey());
                        if ((chat.receiverId.equals(myId) && chat.senderId.equals(otherUserId)) ||
                                (chat.receiverId.equals(otherUserId) && chat.senderId.equals(myId))) {
                            mChat.add(chat);
                        }
                    }
                }
                listener.onMessagesRetrieved(mChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        };
        reference.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    /**
     * Deletes a specific message by its key.
     */
    public void deleteMessage(String messageKey, MessageActionResultListener listener) {
        mDatabase.child("Chats").child(messageKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (listener != null) listener.onSuccess();
            } else {
                if (listener != null) listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Failed to delete message");
            }
        });
    }

    /**
     * Fetches user image from database.
     */
    public void getUserImage(String userId, UserImageListener listener) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theImage = "default";
                if (snapshot.hasChild("imageURL")) {
                    theImage = snapshot.child("imageURL").getValue(String.class);
                } else if (snapshot.hasChild("imageUrl")) {
                    theImage = snapshot.child("imageUrl").getValue(String.class);
                } else if (snapshot.hasChild("avatar")) {
                    theImage = snapshot.child("avatar").getValue(String.class);
                }
                listener.onImageRetrieved(theImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }
}
