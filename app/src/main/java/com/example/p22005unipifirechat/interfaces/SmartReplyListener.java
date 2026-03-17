package com.example.p22005unipifirechat.interfaces;

import java.util.List;

public interface SmartReplyListener {
    void onRepliesGenerated(List<String> suggestions);
    void onError(String errorMessage);
}
