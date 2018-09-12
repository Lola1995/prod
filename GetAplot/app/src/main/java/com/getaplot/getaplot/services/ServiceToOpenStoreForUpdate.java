package com.getaplot.getaplot.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getaplot.getaplot.R;
import com.google.firebase.messaging.RemoteMessage;


public class ServiceToOpenStoreForUpdate extends com.google.firebase.messaging.FirebaseMessagingService {
    //tobe used to tell user to update
    private static final String TAG = "ServiceToOpenStoreForUp";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String tag = remoteMessage.getData().get("update");

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
        try {
            Log.d(TAG, "onMessageReceived: try ran");
            Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_dwnload_url)));

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT

                    );
            mBuilder.setContentIntent(resultPendingIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "onMessageReceived: Catch ran------------");
            Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_dwnload_url)));

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT

                    );
            mBuilder.setContentIntent(resultPendingIntent);

        }


        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (tag.equals("update")) {
            assert mNotifyMgr != null;
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }


    }
}