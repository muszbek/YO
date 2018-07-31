package com.example.tomee.simpleauth;

import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserDataPrivate {

    //public List friendList = new ArrayList();
    public Map friendList = new HashMap();
    public String token;

    public UserDataPrivate() {
        // Default constructor required for calls to DataSnapshot.getValue(UserData.class)
    }

    public UserDataPrivate(String uid) {
        this.friendList.put(uid, new FriendUser(uid));

        token = FirebaseInstanceId.getInstance().getToken();
    }

    public static class FriendUser {

        public String uid;
        public String lastAccessed;

        public FriendUser() {
            // Default constructor required for calls to DataSnapshot.getValue(UserData.class)
        }

        public FriendUser(String uidInput) {
            uid = uidInput;

            String timeStamp = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss").format(new Date());
            lastAccessed = timeStamp;
        }
    }

}
