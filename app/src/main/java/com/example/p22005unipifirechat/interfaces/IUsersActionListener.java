package com.example.p22005unipifirechat.interfaces;
import com.example.p22005unipifirechat.modelclasses.User;

public interface IUsersActionListener {
    void onUserClicked(User user);
    void onDeleteChatClicked(User user);
}
