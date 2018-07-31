package com.example.tomee.simpleauth;

public class UserData {
    public String username = "";
    public String email = "";
    public String uid = "";
    public String photoUrl = "";

    public UserData() {
        // Default constructor required for calls to DataSnapshot.getValue(UserData.class)
    }

    public UserData(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }
}

