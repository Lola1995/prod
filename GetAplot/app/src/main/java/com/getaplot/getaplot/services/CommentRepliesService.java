package com.getaplot.getaplot.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.CommentRepliesActivity;
import com.getaplot.getaplot.ui.MainActivity;
import com.google.firebase.messaging.RemoteMessage;


public class CommentRepliesService extends com.google.firebase.messaging.FirebaseMessagingService {
//FOR ONE REPLYING TO OTHER IN ECOMMENTS

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String CommentId = remoteMessage.getData().get("CommentId");
        String event_id = remoteMessage.getData().get("event_id");
        String uid = remoteMessage.getData().get("uid");
        String commentText = remoteMessage.getData().get("commentText");
        long lastcommentedOn = Long.parseLong(remoteMessage.getData().get("lastcommentedOn"));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message);
        mBuilder.setSound(defaultSoundUri);




        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent resultIntent = new Intent(this,CommentRepliesActivity.class);
        resultIntent.putExtra("CommentId", CommentId);
        resultIntent.putExtra("event_id", event_id);
        resultIntent.putExtra("uid", uid);
        resultIntent.putExtra("commentText", commentText);
        resultIntent.putExtra("lastcommentedOn", lastcommentedOn);

        final PendingIntent pendingIntent1 = PendingIntent.getActivities(this, 0,
                new Intent[] {backIntent, resultIntent}, PendingIntent.FLAG_ONE_SHOT);





        mBuilder.setContentIntent(pendingIntent1);




        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }
}