package com.example.p22005unipifirechat.interfaces;

import com.example.p22005unipifirechat.modelclasses.User;

public interface UserProfileListener {
    void onProfileLoaded(User user);
    void onError(String error);
}
