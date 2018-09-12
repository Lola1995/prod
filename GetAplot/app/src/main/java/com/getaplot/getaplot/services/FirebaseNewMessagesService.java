package com.getaplot.getaplot.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.MainActivity;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseNewMessagesService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String user_id = remoteMessage.getData().get("user_id").toString();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(notification_title).setSound(defaultSoundUri)
                        .setContentText(notification_message);
        //Vibration
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//LED
        mBuilder.setLights(Color.TRANSPARENT, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

//
//        Intent resultIntent = new Intent(click_action);
//        resultIntent.putExtra("user_id", user_id);
//
//        PendingIntent resultPendingIntent =
//                PendingIntent.getActivity(
//                        this,
//                        0,
//                        resultIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//
//        mBuilder.setContentIntent(resultPendingIntent);



        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intent = new Intent(click_action);
        intent.putExtra("user_id", user_id);
//        intent.putExtra("event_name", event_name);

        final PendingIntent pendingIntent1 = PendingIntent.getActivities(this, 0,
                new Intent[] {backIntent, intent}, PendingIntent.FLAG_ONE_SHOT);





        mBuilder.setContentIntent(pendingIntent1);



        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }
}