package com.example.tomee.simpleauth;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;

public class AddFriendActivity extends MainActivityAbstract {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // hiding the add friends button from menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem addFriendsItem = (MenuItem) menu.findItem(R.id.add_friends);
        addFriendsItem.setVisible(false);
        this.invalidateOptionsMenu();

        return true;
    }

    @Override
    public void onUserButtonInteraction(String uid) {
        UserDataPrivate.FriendUser newFriend = new UserDataPrivate.FriendUser(uid);
        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList")
                .child(uid).setValue(newFriend);
    }

    @Override
    protected void friendAddedAction(String uid) {
        tryToExcludeUser(uid);
    }

    @Override
    protected void friendRemovedAction(String uid) {
        tryToDisplayUser(uid);
    }

    @Override
    protected void userAddedAction(String uid) {
        if (friendUids.contains(uid)) {
            tryToExcludeUser(uid);
        } else {
            tryToDisplayUser(uid);
        }
    }



    @Override
    protected void userRemovedAction(String uid) {
        if (friendUids.contains(uid)) {
            friendUids.remove(uid);
            tryToDisplayUser(uid);
        }
    }

    @Override
    protected void getUsersToDisplay() {
        for (String usersToDisplayUid : allUsers.keySet()) {
            if (!friendUids.contains(usersToDisplayUid)) {
                tryToDisplayUser(usersToDisplayUid);
            }
        }
    }
}
