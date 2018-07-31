package com.example.tomee.simpleauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MainActivityAbstract extends AppCompatActivity implements UserView.OnUserButtonListener {

    private TextView messageView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    protected FirebaseUser currentUser;
    protected FirebaseDatabase database;
    protected FirebaseMessaging cloudMessaging;
    protected FirebaseInstanceId instanceId;
    protected Map<String, UserData> allUsers = new HashMap<>();
    protected List<String> friendUids = new ArrayList<>();
    private List<String> usersToDisplay = new ArrayList<>();
    private String filterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        cloudMessaging = FirebaseMessaging.getInstance();
        instanceId = FirebaseInstanceId.getInstance();

        messageView = (TextView) findViewById(R.id.selfTextView);
        messageView.setText("YO, " + getSelfName());

        recyclerView = (RecyclerView) findViewById(R.id.userContainer);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        registerForContextMenu(recyclerView);

        initiateUserList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterName = s;
                reloadUserViews();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterName = s;
                reloadUserViews();
                return false;
            }
        });

        return true;
    }

    private void reloadUserViews() {
        usersToDisplay.clear();
        mAdapter.notifyDataSetChanged();
        getUsersToDisplay();
    }

    abstract protected void getUsersToDisplay();


    @Override
    abstract public void onUserButtonInteraction(String uid);

    public void signOff(MenuItem menuItem) {
        cloudMessaging.unsubscribeFromTopic("yo/" + currentUser.getUid());
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void editProfile(MenuItem menuItem) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void syncFromMenu(MenuItem menuItem) {
        cleanFriendList();
        reloadUserViews();
    }

    public void viewProfile(MenuItem menuItem) {
        String uid = ((UserListAdapter)recyclerView.getAdapter()).getCurrentUid();
        Intent intent = new Intent(this, OthersProfileActivity.class);
        intent.putExtra("selectedUid", uid);
        startActivity(intent);
    }

    public void deleteFriend(MenuItem menuItem) {
        String uid = ((UserListAdapter)recyclerView.getAdapter()).getCurrentUid();
        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList").
                child(uid).setValue(null);
    }

    public void addFriends(MenuItem menuItem) {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }

    private String getSelfName() {
        return !isStringEmpty(currentUser.getDisplayName()) ?
                currentUser.getDisplayName() : currentUser.getEmail();
    }

    private void initiateUserList() {
        mAdapter = new UserListAdapter(usersToDisplay);
        recyclerView.setAdapter(mAdapter);

        listenToAllUsers();
        listenToFriendUids();
    }

    private void listenToFriendUids() {
        DatabaseReference dbRef = database.getReference();
        Query fieldRef = dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList")
                .orderByChild("lastAccessed");


        fieldRef.addChildEventListener(friendUidsChildEventListener);
    }

    private ChildEventListener friendUidsChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            UserDataPrivate.FriendUser friend = dataSnapshot.getValue(UserDataPrivate.FriendUser.class);
            String uid = friend.uid;

            if (!friendUids.contains(uid)) {
                friendUids.add(uid);
                friendAddedAction(uid);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            UserDataPrivate.FriendUser friend = dataSnapshot.getValue(UserDataPrivate.FriendUser.class);
            String uid = friend.uid;
            bumpUser(uid);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            UserDataPrivate.FriendUser friend = dataSnapshot.getValue(UserDataPrivate.FriendUser.class);
            String uid = friend.uid;

            if (friendUids.contains(uid)) {
                friendUids.remove(uid);
                friendRemovedAction(uid);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {
            System.out.println(databaseError.getMessage());
        }
    };

    abstract protected void friendAddedAction(String uid);

    abstract protected void friendRemovedAction(String uid);


    private void listenToAllUsers() {
        DatabaseReference dbRef = database.getReference();
        Query fieldRef = dbRef.child("users").orderByChild("email");

        fieldRef.addChildEventListener(allUidsChildEventListener);
    }

    private ChildEventListener allUidsChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            UserData user = dataSnapshot.getValue(UserData.class);
            String uid = dataSnapshot.getKey();
            allUsers.put(uid, user);

            userAddedAction(uid);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            UserData user = dataSnapshot.getValue(UserData.class);
            String uid = dataSnapshot.getKey();
            allUsers.put(uid, user);

            if (usersToDisplay.contains(uid)) {
                int position = usersToDisplay.indexOf(uid);
                mAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String uid = dataSnapshot.getKey();
            allUsers.remove(uid);

            userRemovedAction(uid);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {
            System.out.println(databaseError.getMessage());
        }
    };

    abstract protected void userAddedAction(String uid);

    abstract protected void userRemovedAction(String uid);


    protected void tryToDisplayUser(String uid) {
        UserData userToAdd = allUsers.get(uid);

        if (userToAdd == null) {
            System.out.println(uid + " has not yet been fetched from public users repository");
        }
        else if (shouldDisplayUser(userToAdd)) {
            usersToDisplay.add(0, userToAdd.uid);
            mAdapter.notifyItemInserted(0);
        }
    }

    protected void bumpUser(String uid) {
        int position = usersToDisplay.indexOf(uid);
        if (position < 0) {
            return;
        }

        usersToDisplay.remove(uid);
        usersToDisplay.add(0, uid);
        mAdapter.notifyItemMoved(position, 0);
    }

    protected void tryToExcludeUser(String uid) {
        UserData userToExclude = allUsers.get(uid);

        if (userToExclude == null) {
            System.out.println(uid + " has not yet been fetched from public users repository");
        }
        else if (usersToDisplay.contains(uid)) {
            int position = usersToDisplay.indexOf(uid);
            usersToDisplay.remove(uid);
            mAdapter.notifyItemRemoved(position);
        }
    }

    private boolean shouldDisplayUser(UserData user) {
        if (usersToDisplay.contains(user.uid)) {
            return false;
        }

        if (isStringEmpty(filterName)) {
            return true;
        }

        if (isUserFilteredOut(user, filterName)) {
            return false;
        } else {
            return true;
        }
    }

    //only reason to keep loading full user data instead of just uid
    private boolean isUserFilteredOut(UserData user, String filter) {
        return ( !(user.username.contains(filter) || user.email.contains(filter)) );
    }

    protected boolean isStringEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    private void cleanFriendList() {
        for (String friendUid : friendUids) {
            if (!allUsers.keySet().contains(friendUid)) {
                DatabaseReference dbRef = database.getReference();
                dbRef.child("usersPrivate").child(currentUser.getUid()).child("friendList")
                        .child(friendUid).setValue(null);
            }
        }
    }

}
