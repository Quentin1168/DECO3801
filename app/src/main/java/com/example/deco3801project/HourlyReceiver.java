package com.example.deco3801project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class HourlyReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //Notification notification = intent.getParcelableExtra(NOTIFICATION);
        // Create the content intent for the notification
        Intent contentIntent = new Intent(context, MainActivity2.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_baseline_local_drink_24)
                .setContentTitle("Drink Water!")
                .setContentText("You should drink some water soon if you haven't yet!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true);

        // Send the Notification
        notificationManager.notify(1, notificationBuilder.build());
    }
}