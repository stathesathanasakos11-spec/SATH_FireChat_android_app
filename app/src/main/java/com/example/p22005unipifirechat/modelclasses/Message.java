package com.example.p22005unipifirechat.modelclasses;

public class Message {

    public String senderId;
    public String receiverId;
    public String messageText;
    public long timestamp;
    public String key;

    public Message() {
    }

    public Message(String senderId, String receiverId, String messageText, long timestamp, String key) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}