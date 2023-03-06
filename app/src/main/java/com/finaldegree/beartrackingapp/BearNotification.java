package com.finaldegree.beartrackingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Map;

public class BearNotification {
    private static final int NOTIFICATION_ID = 1;

    public static void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (for Android Oreo and above)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelId = "BearProximityAlert";
//            CharSequence channelName = "Bear Proximity Alert";
//            String channelDescription = "Alerts users when a bear is close by";
//            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.setDescription(channelDescription);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        Intent myIntent = new Intent(context, MainActivity.class);
//        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                myIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        // Build the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "BearProximityAlert")
//                .setSmallIcon(R.drawable.images)
//                .setContentTitle("Bear Proximity Alert")
//                .setContentText(message)
//                .setWhen(System.currentTimeMillis())
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//        // Show the notification
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}

