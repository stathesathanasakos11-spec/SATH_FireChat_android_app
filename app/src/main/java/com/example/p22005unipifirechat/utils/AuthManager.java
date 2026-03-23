package com.example.p22005unipifirechat.utils;

import androidx.annotation.NonNull;
import com.example.p22005unipifirechat.modelclasses.User;
import com.example.p22005unipifirechat.interfaces.AuthListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//there is only one instance of AuthManager in the app
public class AuthManager {
    private static AuthManager instance;
    //I use mAuth and mDatabase to communicate with Firebase's services
    // mAuth keeps user's track session even after closing the app
    // except user logged out
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    private AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized AuthManager getInstance() {
        // Singleton Design Pattern
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }





    public void signUp(String email, String password, String username, AuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // if email, password & username input are valid then create the new user
                    if (task.isSuccessful()) {
                        // I create a FirebaseUser object and get its UID
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            //save the new user to the Realtime Database
                            User newUser = new User(userId, username, email, "default");
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            listener.onSuccess();
                                        } else {
                                            listener.onFailure(dbTask.getException() != null ? dbTask.getException().getMessage() : "Database Error");
                                        }
                                    });
                        }
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Sign Up Failed");
                    }
                });
    }

    public void signIn(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // if the user is logged in successfully then go to MainActivity
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }





    public void firebaseAuthWithGoogle(AuthCredential credential, AuthListener listener) {
        //Firebase uses Google credentials to authenticate the user
        // this is a Google method to authenticate the user
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // if it comes for the first time, add the user to the database
                            checkAndSaveGoogleUser(user, listener);
                        } else {
                            listener.onFailure("User is null after Google Auth");
                        }
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Authentication Failed");
                    }
                });
    }

    private void checkAndSaveGoogleUser(FirebaseUser firebaseUser, AuthListener listener) {
        DatabaseReference userRef = mDatabase.child("users").child(firebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    //add new user to database
                    String username = firebaseUser.getDisplayName();
                    String email = firebaseUser.getEmail();
                    User newUser = new User(firebaseUser.getUid(), username, email, "simpleuser");
                    userRef.setValue(newUser).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure("Failed to save Google user to database");
                        }
                    });
                } else {
                    // else navigate to MainActivity
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }






    //deletion of the user account
    public void deleteUserAccount(AuthListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // delete user from users node in Realtime database
            mDatabase.child("users").child(uid).removeValue().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    // also delete user from Firebase Authentication
                    user.delete().addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure(authTask.getException() != null ? authTask.getException().getMessage() : "Auth Delete Failed");
                        }
                    });
                } else {
                    listener.onFailure(dbTask.getException() != null ? dbTask.getException().getMessage() : "Database removal failed");
                }
            });
        } else {
            listener.onFailure("No user is currently signed in");
        }
    }

    public void signOut() {
        // after user press logout button and confirms his choice
        // he is signed out from the app and Firebase does not keep track of the user
        mAuth.signOut();
    }
}
