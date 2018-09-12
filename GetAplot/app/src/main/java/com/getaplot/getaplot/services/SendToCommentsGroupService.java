package com.getaplot.getaplot.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.EventCommentsActivity;
import com.getaplot.getaplot.ui.MainActivity;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Elia on 9/8/2017.
 */

public class SendToCommentsGroupService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();
        String event_id = remoteMessage.getData().get("event_id");
        String event_name = remoteMessage.getData().get("event_name");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setAutoCancel(true)
                .setContentTitle(notification_title).setSound(defaultSoundUri)
                .setContentText(notification_message)
                .setWhen(System.currentTimeMillis());
        //Vibration
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//LED
        mBuilder.setLights(Color.TRANSPARENT, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

//
//        Intent resultIntent = new Intent(this, EventCommentsActivity.class);
//        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
//        taskStackBuilder.addNextIntent(resultIntent);
//
//        resultIntent.putExtra("event_id", event_id);
//        resultIntent.putExtra("event_name", event_name);
//
//        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(
//
//                0,
//                PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//


        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intent = new Intent(this, EventCommentsActivity.class);
        intent.putExtra("event_id", event_id);
       intent.putExtra("event_name", event_name);

        final PendingIntent pendingIntent1 = PendingIntent.getActivities(this, 0,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_ONE_SHOT);





        mBuilder.setContentIntent(pendingIntent1);

        int id = (int) System.currentTimeMillis();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(id, mBuilder.build());


    }
}
