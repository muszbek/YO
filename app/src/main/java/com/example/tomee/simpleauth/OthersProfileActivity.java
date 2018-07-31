package com.example.tomee.simpleauth;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class OthersProfileActivity extends ProfileActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        uid = intent.getExtras().getString("selectedUid");

        super.onCreate(savedInstanceState);
        ((Button) findViewById(R.id.editUserNameButton)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.changePicButton)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.changePasswordButton)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.deleteButton)).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void getProfilePic() {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(uid).child("photoUrl").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String picUrl = dataSnapshot.getValue(String.class);
                        getProfilePicFromUri(picUrl);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    @Override
    protected void getEmail() {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(uid).child("email").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String email = dataSnapshot.getValue(String.class);
                        getEmailFromString(email);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    @Override
    protected void getUserName() {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(uid).child("username").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.getValue(String.class);
                        getUserNameFromString(userName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    protected void getLastInteract() {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList")
                .child(uid).child("lastAccessed")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lastInteractTextView = (TextView) findViewById(R.id.lastInteractionTextView);
                        String timestamp = dataSnapshot.getValue(String.class);

                        if (timestamp == null) {
                            lastInteractTextView.setVisibility(View.INVISIBLE);
                        } else {
                            String timeLabel = getResources().getString(R.string.last_interaction);
                            lastInteractTextView.setText(timeLabel + " " + timestamp);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }
}
