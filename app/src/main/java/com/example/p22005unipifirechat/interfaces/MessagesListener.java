package com.example.p22005unipifirechat.interfaces;

import com.example.p22005unipifirechat.modelclasses.Message;
import java.util.List;

public interface MessagesListener {
    void onMessagesRetrieved(List<Message> messages);
    void onError(String error);
}
