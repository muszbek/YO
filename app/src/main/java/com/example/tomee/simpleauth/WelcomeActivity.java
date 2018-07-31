package com.example.tomee.simpleauth;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
//TODO: do not download pictures again and again
public class WelcomeActivity extends MainActivityAbstract {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onUserButtonInteraction(String uid) {
        System.out.println("YO " + uid);
        updateTimestamp(uid);

        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(uid).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String targetToken = dataSnapshot.getValue(String.class);
                String selfName = !isStringEmpty(currentUser.getDisplayName()) ?
                        currentUser.getDisplayName() : currentUser.getEmail();
                String selfUid = currentUser.getUid();
                sendYo(targetToken, selfName, selfUid);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    private void sendYo(String targetToken, String selfName, String selfUid) {
        new SendYoHttp().execute(targetToken, selfName, selfUid);
    }

    private void updateTimestamp(String targetUid) {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss").format(new Date());

        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList")
                .child(targetUid).child("lastAccessed")
                .setValue(timeStamp);
    }

    @Override
    protected void friendAddedAction(String uid) {
        tryToDisplayUser(uid);
    }

    @Override
    protected void friendRemovedAction(String uid) {
        tryToExcludeUser(uid);
    }

    @Override
    protected void userAddedAction(String uid) {
        if (friendUids.contains(uid)) {
            tryToDisplayUser(uid);
        }
    }

    @Override
    protected void userRemovedAction(String uid) {
        if (friendUids.contains(uid)) {
            friendUids.remove(uid);
            tryToExcludeUser(uid);
        }
    }

    @Override
    protected void getUsersToDisplay() {
        for (String usersToDisplayUid : allUsers.keySet()) {
            if (friendUids.contains(usersToDisplayUid)) {
                tryToDisplayUser(usersToDisplayUid);
            }
        }
    }

}