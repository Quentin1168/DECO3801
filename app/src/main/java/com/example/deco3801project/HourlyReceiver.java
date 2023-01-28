package com.example.deco3801project;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class HourlyReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the content intent for the notification
        Intent contentIntent = new Intent(context, WaterIntake.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the Notification
        long[] vibrationPattern = {1000 , 1000 , 1000 , 1000};
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_baseline_local_drink_24)
                .setContentTitle("Drink Water!")
                .setContentText("You should drink some water soon if you haven't yet!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setVibrate(vibrationPattern)
                .setAutoCancel(true);

        // Send the Notification
        notificationManager.notify(1, notificationBuilder.build());
    }
}
