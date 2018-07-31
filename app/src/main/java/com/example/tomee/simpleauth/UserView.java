package com.example.tomee.simpleauth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserView extends FrameLayout {

    public String mUid;

    private Button userActionButton;
    private TextView userNameTextView;
    private ImageView userProfilePic;

    private FirebaseDatabase database;
    private OnUserButtonListener mListener;

    // declare all constructors, to make sure they are created at automatic generation
    public UserView(@NonNull Context context) {
        super(context);
    }

    public UserView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UserView(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public UserView(@NonNull Context context, String uid) {
        super(context);
        construct(context, uid);
    }

    // part of constructor, has to be called manually
    public void construct(Context context, String uid) {
        mUid = uid;
        database = FirebaseDatabase.getInstance();

        listenToPic();
        listenToData();
        initButton(context);
    }

    private void listenToPic() {
        userProfilePic = (ImageView) findViewById(R.id.userProfileImageView);

        DatabaseReference dbRef = database.getReference();
        Query fieldRef = dbRef.child("users").child(mUid).child("photoUrl");

        fieldRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String mPicUrl = dataSnapshot.getValue(String.class);
                if (isStringValid(mPicUrl)) {
                    new DownloadImageTask(userProfilePic).execute(mPicUrl);
                } else {
                    userProfilePic.setImageResource(R.drawable.ic_user_default);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    private void listenToData() {
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);

        DatabaseReference dbRef = database.getReference();
        Query fieldRef = dbRef.child("users").child(mUid);

        fieldRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData userData = dataSnapshot.getValue(UserData.class);
                String mUserName = isStringValid(userData.username) ? userData.username : userData.email;
                if (isStringValid(mUserName)) {
                    userNameTextView.setText(mUserName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    private void initButton(Context context) {
        attachListener(context);

        userActionButton = (Button) findViewById(R.id.userActionButton);
        userActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });

        if (context instanceof AddFriendActivity) {
            userActionButton.setText("+");
        }
    }

    private boolean isStringValid(String string) {
        return (string != null && !string.isEmpty());
    }

    private void attachListener(Context context) {
        if (context instanceof OnUserButtonListener) {
            mListener = (OnUserButtonListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserButtonListener");
        }
    }

    private void onButtonPressed() {
        if (mListener != null) {
            mListener.onUserButtonInteraction(mUid);
        }
    }

    // implement by parent
    public interface OnUserButtonListener {
        void onUserButtonInteraction(String uid);
    }
}
