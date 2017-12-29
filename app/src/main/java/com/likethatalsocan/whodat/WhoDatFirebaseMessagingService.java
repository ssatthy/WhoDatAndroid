package com.likethatalsocan.whodat;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by sathy on 29/12/17.
 */

public class WhoDatFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {

        }
        if (remoteMessage.getNotification() != null) {
            System.out.print("Message Notification Body: " + remoteMessage.getNotification().getBody());
            System.out.print("Message Notification Title: " + remoteMessage.getNotification().getTitle());

            NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0 , notifiBuilder.build());

        }
    }
}
