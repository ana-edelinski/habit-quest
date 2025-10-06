package com.example.habitquest.presentation.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.presentation.services.AllianceNotificationService;
import com.example.habitquest.utils.RepositoryCallback;

public class AllianceInviteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String allianceId = intent.getStringExtra("allianceId");
        String allianceName = intent.getStringExtra("allianceName");
        String inviterName = intent.getStringExtra("inviterName");

        if (action == null || allianceId == null) return;

        AllianceRepository repo = new AllianceRepository(context);

        if (action.equals("ACCEPT_INVITE")) {
            repo.acceptAllianceInvite(allianceId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(context, "You joined the alliance!", Toast.LENGTH_SHORT).show();

                    NotificationManagerCompat.from(context).cancel(allianceId.hashCode());

                    stopServiceSafely(context, allianceId);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (action.equals("REJECT_INVITE")) {
            repo.rejectAllianceInvite(allianceId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show();
                    NotificationManagerCompat.from(context).cancel(allianceId.hashCode());
                    stopServiceSafely(context, allianceId);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void stopServiceSafely(Context context, String allianceId) {
        if (AllianceNotificationService.isRunning(allianceId)) {
            AllianceNotificationService.removeActive(allianceId);
            Intent serviceIntent = new Intent(context, AllianceNotificationService.class);
            context.stopService(serviceIntent);
        }
    }

}
