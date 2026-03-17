package com.example.p22005unipifirechat.utils;

import static android.provider.Settings.System.getString;

import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.utils.ChatManager;
import com.example.p22005unipifirechat.activities.ChatActivity;
import com.example.p22005unipifirechat.interfaces.SmartReplyListener;
import com.example.p22005unipifirechat.modelclasses.Message;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.firebase.database.DataSnapshot;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;

public class SmartReplyManager {
    //Singleton Design Pattern
    private static SmartReplyManager instance;
    private final SmartReplyGenerator smartReplyGenerator;

    private SmartReplyManager() {
        // Αρχικοποίηση του AI Smart Reply Generator της Google
        smartReplyGenerator = SmartReply.getClient();
    }

    public static synchronized SmartReplyManager getInstance() {
        if (instance == null) {
            instance = new SmartReplyManager();
        }
        return instance;
    }




    public void generateReplies(List<Message> chatHistory, String currentUserId, SmartReplyListener listener ){
        List<TextMessage> conversation = new ArrayList<>();

        // χρησιμοποιώ τα τελευταία 10 μηνύματα
        int start = Math.max(0, chatHistory.size() - 10);

        for (int i = start; i < chatHistory.size(); i++) {
            Message msg = chatHistory.get(i);

            //το ML Kit παράγει μήνυμα οπότε πρέπει να ξέρει ποιος χρήστης θα το χρησιμοποιήσει
            if (msg.senderId.equals(currentUserId)) {
                conversation.add(TextMessage.createForLocalUser(msg.messageText, msg.timestamp));
            } else {
                conversation.add(TextMessage.createForRemoteUser(msg.messageText, msg.timestamp, msg.senderId));
            }
        }

        try {
            smartReplyGenerator.suggestReplies(conversation)
                    .addOnSuccessListener(result -> {
                        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // Το ML Kit υποστηρίζει κυρίως Αγγλικά
                            listener.onError(" ");
                        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            List<String> replies = new ArrayList<>();
                            for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                replies.add(suggestion.getText());
                            }
                            listener.onRepliesGenerated(replies);
                        } else {
                            listener.onError(" ");
                        }
                    })
                    .addOnFailureListener(e -> {
                        listener.onError(e.getMessage());
                    });

        } catch (Exception e) {
            // για να μην κρασσάρει το ChatActivity σε σφάλματατα του smartReplyGenerator
            listener.onError(" ");
        }
    }

    public void close(){
        //αποδέσμευση πόρων συστήματος όταν κλείνει η συνομιλία
        if (smartReplyGenerator != null) {
            smartReplyGenerator.close();
        }
        //όταν κλείνει η chatActivity ουσιαστικά καταστρέφεται το instance του smartreplygenerator
        // το κάνω null ώστε αν έχει καταστραφεί να το δει το Android και να το χτίσει ξανά
        instance = null;
    }
}
