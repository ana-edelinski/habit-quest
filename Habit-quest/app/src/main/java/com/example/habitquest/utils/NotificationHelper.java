package com.example.habitquest.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habitquest.R;
import com.example.habitquest.presentation.receivers.AllianceInviteReceiver;

public class NotificationHelper {

    public static final String CHANNEL_ID = "alliance_notifications";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alliance Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for alliance invitations and responses");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void showAllianceInvite(Context context, String inviterName, String allianceName, String allianceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent acceptIntent = new Intent(context, AllianceInviteReceiver.class);
        acceptIntent.setAction("ACCEPT_INVITE");
        acceptIntent.putExtra("allianceId", allianceId);
        acceptIntent.putExtra("allianceName", allianceName);
        acceptIntent.putExtra("inviterName", inviterName);

        Intent rejectIntent = new Intent(context, AllianceInviteReceiver.class);
        rejectIntent.setAction("REJECT_INVITE");
        rejectIntent.putExtra("allianceId", allianceId);
        rejectIntent.putExtra("allianceName", allianceName);
        rejectIntent.putExtra("inviterName", inviterName);

        PendingIntent acceptPending = PendingIntent.getBroadcast(
                context, allianceId.hashCode(), acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent rejectPending = PendingIntent.getBroadcast(
                context, allianceId.hashCode() + 1, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alliance Invitation")
                .setContentText("You have been invited to join \"" + allianceName + "\" by " + inviterName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setTimeoutAfter(0)
                .setDeleteIntent(null)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_check, "Accept", acceptPending)
                .addAction(R.drawable.ic_close, "Reject", rejectPending);


        NotificationManagerCompat.from(context).notify(allianceId.hashCode(), builder.build());
    }

    public static void showAllianceAccepted(Context context, String memberName, String allianceName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alliance Update")
                .setContentText(memberName + " has joined \"" + allianceName + "\"")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void showAllianceChatMessage(Context context, String senderName, String messageText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nova poruka od " + senderName)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

}
