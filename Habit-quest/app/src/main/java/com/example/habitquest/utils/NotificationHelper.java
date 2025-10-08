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

    // 🔹 Posebni kanali
    public static final String CHANNEL_INVITES = "alliance_invites_channel";
    public static final String CHANNEL_CHAT = "alliance_chat_channel";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 🔹 Kanal za pozive / pozivnice
            NotificationChannel invites = new NotificationChannel(
                    CHANNEL_INVITES,
                    "Alliance Invitations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            invites.setDescription("Notifications for alliance invitations and responses");
            invites.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(invites);

            // 🔹 Kanal za chat poruke
            NotificationChannel chat = new NotificationChannel(
                    CHANNEL_CHAT,
                    "Alliance Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            chat.setDescription("Notifications for new alliance chat messages");
            chat.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(chat);
        }
    }

    // 🔹 Pozivnica
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
                context, allianceId.hashCode(), acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent rejectPending = PendingIntent.getBroadcast(
                context, allianceId.hashCode() + 1, rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INVITES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alliance Invitation")
                .setContentText("You have been invited to join \"" + allianceName + "\" by " + inviterName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_check, "Accept", acceptPending)
                .addAction(R.drawable.ic_close, "Reject", rejectPending);

        NotificationManagerCompat.from(context).notify(allianceId.hashCode(), builder.build());
    }

    // 🔹 Chat poruka
    public static void showAllianceChatMessage(Context context, String senderName, String messageText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHAT)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(senderName)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(false);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    // 🔹 Kada neko prihvati pozivnicu
    public static void showAllianceAccepted(Context context, String memberName, String allianceName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INVITES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alliance Update")
                .setContentText(memberName + " has joined \"" + allianceName + "\"")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }
}
