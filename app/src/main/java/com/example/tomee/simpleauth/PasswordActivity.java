package com.example.tomee.simpleauth;

import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private CheckBox showPasswordCheckBox;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private PasswordTransformationMethod pwTransform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        showPasswordCheckBox = (CheckBox) findViewById(R.id.showPasswordCheckBox);
        currentPasswordEditText = (EditText) findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = (EditText) findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        pwTransform = new PasswordTransformationMethod();
    }

    public void showPasswordToggle(View view) {
        if (showPasswordCheckBox.isChecked()) {
            currentPasswordEditText.setTransformationMethod(null);
            newPasswordEditText.setTransformationMethod(null);
            confirmPasswordEditText.setTransformationMethod(null);
        } else {
            currentPasswordEditText.setTransformationMethod(pwTransform);
            newPasswordEditText.setTransformationMethod(pwTransform);
            confirmPasswordEditText.setTransformationMethod(pwTransform);
        }
    }

    public void changePassword(View view) {
        String currentPW = currentPasswordEditText.getText().toString();
        String newPW = newPasswordEditText.getText().toString();
        String confirmPW = confirmPasswordEditText.getText().toString();

        if (!isPasswordValid(currentPW)) {
            Toast.makeText(this, R.string.pw_toast_current_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPW);
        // Prompt the user to re-provide their sign-in credentials
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println(getString(R.string.pw_toast_reauthenticated));
                    }
                });

        if (!newPW.equals(confirmPW)) {
            Toast.makeText(this, R.string.pw_toast_new_confirm_different,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPW.equals(currentPW)) {
            Toast.makeText(this, R.string.pw_toast_new_old_same,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordValid(newPW)) {
            Toast.makeText(this, R.string.pw_toast_new_invalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.updatePassword(newPW)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PasswordActivity.this, R.string.pw_toast_updated, Toast.LENGTH_SHORT).show();
                            NavUtils.navigateUpFromSameTask(PasswordActivity.this);
                        }
                    }
                });
    }

    private boolean isPasswordValid(String pw) {
        return (pw != null && pw.length() >= 8);
    }
}
