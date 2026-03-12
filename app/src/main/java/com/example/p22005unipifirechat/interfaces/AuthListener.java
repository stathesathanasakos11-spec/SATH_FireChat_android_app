package com.example.p22005unipifirechat.interfaces;

public interface AuthListener {
    // επικοινωνία με το LoginActivity για το τι γίνεται αν πέτυχε ή απέτυχε το authentication του χρήστη
    void onSuccess();
    void onFailure(String message);
}
