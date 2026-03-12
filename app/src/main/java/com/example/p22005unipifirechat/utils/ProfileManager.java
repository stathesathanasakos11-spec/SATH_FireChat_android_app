package com.example.p22005unipifirechat.utils;

import androidx.annotation.NonNull;

import com.example.p22005unipifirechat.modelclasses.User;
import com.example.p22005unipifirechat.interfaces.UserProfileListener;
import com.example.p22005unipifirechat.interfaces.ProfileUpdateListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileManager {

    private static ProfileManager instance;
    private final DatabaseReference mDatabase;

    private ProfileManager() {
        //απευθείας σύνδεση με τον κόμβο users στη Realtime database
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    public static synchronized ProfileManager getInstance() {
        if (instance == null) {
            //singleton design pattern
            instance = new ProfileManager();
        }
        return instance;
    }




    public void loadUserProfile(String userId, UserProfileListener listener) {
        //πρόσβαση απευθείας στο κόμβο του χρήστη στη βάση δεδομένων
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            //όταν αλλάζουν τα δεδομένα του χρήστη (πχ avatar/username)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    //φορτώνω όλα τα στοιχεία του χρήστη και το χρησιμοποιώ στο Activity
                    listener.onProfileLoaded(user);
                } else {
                    listener.onError("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }


    public void updateProfile(String userId, String newUsername, String avatarId, ProfileUpdateListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("imageUrl", avatarId);

        mDatabase.child(userId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (listener != null) listener.onSuccess();
            } else {
                if (listener != null) listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Update failed");
            }
        });
    }
}
