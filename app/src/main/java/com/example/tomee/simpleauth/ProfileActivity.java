package com.example.tomee.simpleauth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    protected FirebaseUser currentUser;
    protected StorageReference storageRef;
    protected FirebaseDatabase database;
    protected ImageView profilePic;
    protected TextView emailTextView;
    protected TextView userNameTextView;
    protected TextView lastInteractTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

        getProfilePic();
        getEmail();
        getUserName();
        getLastInteract();
    }

    public void changePassword(View view) {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
    }


    public void changeUserName(View view) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(currentUser.getDisplayName());
        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_username)
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newUserName = input.getText().toString();
                        updateUserNameInAuth(newUserName);
                        updateUserNameInDB(newUserName);
                        getUserNameFromString(newUserName);
                    }})
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void updateUserNameInAuth(String userName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName).build();

        updateProfil(profileUpdates);
    }

    private void updateUserNameInDB(String newUserName) {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(currentUser.getUid()).child("username").setValue(newUserName);
    }

    private void updateProfil(UserProfileChangeRequest profileUpdates) {
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Update auth reference to pic successful");
                        } else {
                            System.out.println("Update auth reference to pic FAILED");
                        }
                    }
                });
    }


    public void changePic(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri targetUri = data.getData();
        profilePic.setImageURI(targetUri);

        StorageReference profPicRef = storageRef.child("profPics/" + currentUser.getUid() + ".jpg");
        profPicRef.putFile(targetUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ProfileActivity.this, R.string.upload_message_success,
                                Toast.LENGTH_SHORT).show();
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        updatePicInAuth(downloadUrl);
                        updatePicInDB(downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ProfileActivity.this, R.string.upload_message_failure,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePicInAuth(Uri downloadUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUrl).build();

        updateProfil(profileUpdates);
    }

    private void updatePicInDB(Uri downloadUrl) {
        String url = downloadUrl.toString();
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(currentUser.getUid()).child("photoUrl").setValue(url);
    }


    public void deleteUser(View view) {
        final EditText pwInput = new EditText(this);
        pwInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_user)
                .setMessage(R.string.delete_user_confirm_message)
                .setView(pwInput)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String password = pwInput.getText().toString();
                        if (!isStringValid(password)) {
                            return;
                        }

                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                        currentUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        currentUser.delete().addOnCompleteListener(deleteUserCompleteListener);
                                    }
                                });

                        System.out.println("Delete clicked");

                    }})
                .setNegativeButton(R.string.cancel, null).show();
    }

    private OnCompleteListener deleteUserCompleteListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                deleteUserFromDB();
                deleteProfilePic();
                Toast.makeText(ProfileActivity.this, R.string.user_deleted,
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    };

    private void deleteUserFromDB() {
        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(currentUser.getUid()).setValue(null);
        dbRef.child("usersPrivate").child(currentUser.getUid()).setValue(null);
    }

    private void deleteProfilePic() {
        storageRef.child("profPics/" + currentUser.getUid() + ".jpg").delete();
    }


    protected void getProfilePic() {
        Uri photoUri = currentUser.getPhotoUrl();

        if (photoUri == null) {
            return;
        }

        getProfilePicFromUri(photoUri.toString());
    }

    protected void getProfilePicFromUri(String mPicUrl) {
        profilePic = (ImageView) findViewById(R.id.profileImageView);

        if (isStringValid(mPicUrl)) {
            new DownloadImageTask(profilePic).execute(mPicUrl);
        }
    }

    protected void getEmail() {
        String email = currentUser.getEmail();
        getEmailFromString(email);
    }

    protected void getEmailFromString(String email) {
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        emailTextView.setText(email);
    }

    protected void getUserName() {
        String userName = currentUser.getDisplayName();
        getUserNameFromString(userName);
    }

    protected void getUserNameFromString(String userName) {
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        if (isStringValid(userName)) {
            userNameTextView.setText(userName);
        } else {
            userNameTextView.setText(emailTextView.getText());
        }
    }

    protected boolean isStringValid(String string) {
        return (string != null && !string.isEmpty());
    }

    protected void getLastInteract() {
        lastInteractTextView = (TextView) findViewById(R.id.lastInteractionTextView);
        lastInteractTextView.setVisibility(View.INVISIBLE);
    }
}
