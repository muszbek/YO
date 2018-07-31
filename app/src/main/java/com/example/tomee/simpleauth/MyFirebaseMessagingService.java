package com.example.tomee.simpleauth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseDatabase database;
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String senderName = remoteMessage.getData().get("senderName");
        String senderUid = remoteMessage.getData().get("senderUid");
        if (senderName == null) {
            return;
        }

        System.out.println("Notification received");

        Uri soundURI = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.yo_sound);
        String channelID = getResources().getString(R.string.default_notification_channel_id);
        String notifText = getResources().getString(R.string.by) + " " + senderName;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                getApplicationContext(), channelID)
                .setContentTitle(getResources().getString(R.string.yo))
                .setSmallIcon(R.drawable.ic_yo)
                .setContentText(notifText)
                .setAutoCancel(true)
                .setSound(soundURI)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationID.getID(), notificationBuilder.build());

        System.out.println("--> Message from " + senderName);
        updateTimestamp(senderUid);
        popupYo(senderName);
    }

    private void updateTimestamp(String senderUid) {
        String selfUid = currentUser.getUid();
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss").format(new Date());

        DatabaseReference dbRef = database.getReference();
        dbRef.child("usersPrivate").child(selfUid).child("friendList").child(senderUid)
                .child("lastAccessed").setValue(timeStamp);

        dbRef.child("usersPrivate").child(selfUid).child("friendList").child(senderUid)
                .child("uid").setValue(senderUid);
    }

    private void popupYo(String senderName) {
        Intent intent = new Intent(this, YoPopup.class);
        intent.putExtra("SENDER", senderName);
        startActivity(intent);
    }
}

class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}