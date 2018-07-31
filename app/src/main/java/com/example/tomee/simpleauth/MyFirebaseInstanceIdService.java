package com.example.tomee.simpleauth;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseInstanceId instanceId;


    @Override
    public void onCreate() {
        database = FirebaseDatabase.getInstance();
        instanceId = FirebaseInstanceId.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }

                sendTokenToDB();
            }
        });
    }

    @Override
    public void onTokenRefresh() {
        if (currentUser == null) {
            return;
        }

        sendTokenToDB();
    }

    private void sendTokenToDB() {
        String token = instanceId.getToken();

        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(currentUser.getUid()).child("token").setValue(token);
    }

}
