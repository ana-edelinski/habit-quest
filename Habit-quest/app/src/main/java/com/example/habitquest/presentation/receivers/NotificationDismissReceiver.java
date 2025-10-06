package com.example.habitquest.presentation.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.habitquest.presentation.services.AllianceNotificationService;

public class NotificationDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String allianceId = intent.getStringExtra("allianceId");
        String allianceName = intent.getStringExtra("allianceName");
        String inviterName = intent.getStringExtra("inviterName");

        Log.d("AllianceInvite", "Notification dismissed – recreating...");

        Intent serviceIntent = new Intent(context, AllianceNotificationService.class);
        serviceIntent.putExtra(AllianceNotificationService.EXTRA_ALLIANCE_ID, allianceId);
        serviceIntent.putExtra(AllianceNotificationService.EXTRA_ALLIANCE_NAME, allianceName);
        serviceIntent.putExtra(AllianceNotificationService.EXTRA_INVITER_NAME, inviterName);

        serviceIntent.setAction("com.example.habitquest.RECREATE_NOTIFICATION");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

}
