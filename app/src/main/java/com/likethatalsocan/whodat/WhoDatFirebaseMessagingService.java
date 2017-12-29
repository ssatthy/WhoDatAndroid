package com.likethatalsocan.whodat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

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
            System.out.println("Message Notification Title: " + remoteMessage.getNotification().getTitle());

            if(remoteMessage.getNotification().getTitle().startsWith("Chat ended")) {
                createNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            }

        }
    }

    public void createNotification(String title, String message){

        Intent intent = new Intent(this, MessageActivity.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MessageActivity.class);
        taskStackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder.
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }
}
