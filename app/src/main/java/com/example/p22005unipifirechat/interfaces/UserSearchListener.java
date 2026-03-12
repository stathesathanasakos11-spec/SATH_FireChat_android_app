package com.example.p22005unipifirechat.interfaces;

public interface UserSearchListener {
    void onUserFound(String uid, String username);
    void onUserNotFound();
    void onError(String error);
}
