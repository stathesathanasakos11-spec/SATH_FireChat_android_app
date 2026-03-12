package com.example.p22005unipifirechat.interfaces;

import com.example.p22005unipifirechat.modelclasses.User;
import java.util.List;

public interface ChatListListener {
    void onChatListUpdated(List<User> users);
    void onEmpty();
    void onError(String error);
}
