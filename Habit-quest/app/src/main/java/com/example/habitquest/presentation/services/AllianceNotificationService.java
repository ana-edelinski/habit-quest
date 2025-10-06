package com.example.habitquest.presentation.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.habitquest.R;
import com.example.habitquest.presentation.receivers.AllianceInviteReceiver;
import com.example.habitquest.presentation.receivers.NotificationDismissReceiver;

import java.util.HashSet;
import java.util.Set;

public class AllianceNotificationService extends Service {

    public static final String CHANNEL_ID = "alliance_foreground_channel";
    private static final int BASE_NOTIFICATION_ID = 9000;
    private static final Set<String> activeServices = new HashSet<>();

    public static final String EXTRA_ALLIANCE_ID = "allianceId";
    public static final String EXTRA_ALLIANCE_NAME = "allianceName";
    public static final String EXTRA_INVITER_NAME = "inviterName";

    public static boolean isRunning(String key) {
        return activeServices.contains(key);
    }

    public static void removeActive(String key) {
        synchronized (activeServices) {
            activeServices.remove(key);
            activeServices.notifyAll();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String allianceId = intent.getStringExtra(EXTRA_ALLIANCE_ID);
        String allianceName = intent.getStringExtra(EXTRA_ALLIANCE_NAME);
        String inviterName = intent.getStringExtra(EXTRA_INVITER_NAME);
        if (allianceId == null) return START_NOT_STICKY;

        activeServices.add(allianceId);

        PendingIntent acceptPending = getAcceptPendingIntent(allianceId, allianceName, inviterName);
        PendingIntent rejectPending = getRejectPendingIntent(allianceId, allianceName, inviterName);

        Intent dismissIntent = new Intent(this, NotificationDismissReceiver.class);
        dismissIntent.setAction("com.example.habitquest.NOTIFICATION_DISMISSED");
        dismissIntent.putExtra("allianceId", allianceId);
        dismissIntent.putExtra("allianceName", allianceName);
        dismissIntent.putExtra("inviterName", inviterName);

        PendingIntent dismissPending = PendingIntent.getBroadcast(
                this,
                allianceId.hashCode() + 2,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alliance Invitation")
                .setContentText("You have been invited to join \"" + allianceName + "\" by " + inviterName)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(dismissPending)
                .addAction(R.drawable.ic_check, "Accept", acceptPending)
                .addAction(R.drawable.ic_close, "Reject", rejectPending)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                    BASE_NOTIFICATION_ID + allianceId.hashCode(),
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            );
        } else {
            startForeground(BASE_NOTIFICATION_ID + allianceId.hashCode(), notification);
        }

        return START_STICKY;
    }

    private PendingIntent getAcceptPendingIntent(String id, String name, String inviter) {
        Intent i = new Intent(this, AllianceInviteReceiver.class);
        i.setAction("ACCEPT_INVITE");
        i.putExtra("allianceId", id);
        i.putExtra("allianceName", name);
        i.putExtra("inviterName", inviter);
        return PendingIntent.getBroadcast(
                this, id.hashCode(), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent getRejectPendingIntent(String id, String name, String inviter) {
        Intent i = new Intent(this, AllianceInviteReceiver.class);
        i.setAction("REJECT_INVITE");
        i.putExtra("allianceId", id);
        i.putExtra("allianceName", name);
        i.putExtra("inviterName", inviter);
        return PendingIntent.getBroadcast(
                this, id.hashCode() + 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Alliance Invitations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Persistent alliance invitations");
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ch.setShowBadge(false);
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(ch);
        }
    }
}
