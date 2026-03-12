package com.example.p22005unipifirechat.modelclasses;

public class User {
    public String uid;
    public String username;
    public String email;
    public long lastMsgTime;
    public String imageUrl;

    //empty constructor for firebase
    public User() {
    }

    public User(String uid, String username, String email, String imageUrl) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    //getters and setters
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return imageUrl;
    }
}
