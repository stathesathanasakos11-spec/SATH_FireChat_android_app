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

// Singleton Design Pattern for AuthManager & SingleResponsibility Principle
// μόνο ένα instance στην εφαρμογή γι επικοινωνία με το Firebase Authentication και τη Realtime Database
public class AuthManager {
    private static AuthManager instance;
    //το mAuth είναι αυτό που κρατάει την κατάσταση του χρήστη
    // χρησιμοποιείται για το login και κρατάει τα στοιχεία του χρήστη και μετά το κλείσιμο της εφαρμογής
    // εκτός αν ο ίδιος κάνει αποσύνδεση
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
                    // ολοκληρώθηκε ο ορισμός email, username & password και έγινε η διαδικασία από τη Firebase
                    if (task.isSuccessful()) {
                        // φτιάχνω νέο αντικείμενο τύπου FirebaseUser και παίρνω τα στοιχεία του μέσω του mAuth
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // δημιουργία νέου χρήστη στη Realtime Database και αποθήκευση
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
                        // αν συμπλήρωσε σωστάτα στοιχεία η LoginActivity τον μεταφέρει στην MainActivity (home page)
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }






    public void firebaseAuthWithGoogle(AuthCredential credential, AuthListener listener) {
        // έτοιμη μέθοδος της Firebase-Google μέσω των credentials του χρήστη
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // αν είναι νέος χρήστης πρέπει να προστεθεί στη Realtime Database
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
                    //προσθήκη νέου χρήστη στη βάση δεδομένων
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
                    // Διαφορετικά απλά πάμε στην MainActivity από την LoginActivity
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }







    // διαγραφή λογαριασμού χρήστη από την εφαρμογή
    public void deleteUserAccount(AuthListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // διαγραφή του χρήστη με το UID του από τον κόμβο users της Realtime Database
            mDatabase.child("users").child(uid).removeValue().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    // διαγραφή και από το Firebase Authentication platform
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
        // μετά από logout δεν κρατιούνται τα στοιχεία του χρήστη στην εφαρμογή
        mAuth.signOut();
    }
}
