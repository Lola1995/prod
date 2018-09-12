package com.getaplot.getaplot.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.getaplot.getaplot.R;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessaging extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // The id of the channel.
//        String id = "my_channel_01";
//        // The user-visible name of the channel.
//        CharSequence name = getString(R.string.channel_name);
//        // The user-visible description of the channel.
//        String description = getString(R.string.channel_description);
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
//// Configure the notification channel.
//        mChannel.setDescription(description);
//        mChannel.enableLights(true);
//// Sets the notification light color for notifications posted to this
//// channel, if the device supports this feature.
//        mChannel.setLightColor(Color.RED);
//        mChannel.enableVibration(true);
//        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//        mNotificationManager.createNotificationChannel(mChannel);
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String user_id = remoteMessage.getData().get("user_id").toString();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setSound(defaultSoundUri)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message);
        //Vibration
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//LED
        mBuilder.setLights(Color.TRANSPARENT, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        // mBuilder.setChannelId(id);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", user_id);
        resultIntent.putExtra("friendsnow", "friendsnow");


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }
}