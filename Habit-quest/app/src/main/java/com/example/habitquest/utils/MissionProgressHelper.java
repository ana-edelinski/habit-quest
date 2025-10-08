package com.example.habitquest.utils;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MissionAction;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceMissionViewModelFactory;

public class MissionProgressHelper {

    private static void reportProgress(Context context, MissionAction action) {
        try {
            AllianceMissionViewModel missionVM =
                    new ViewModelProvider((ViewModelStoreOwner) context, new AllianceMissionViewModelFactory(context))
                            .get(AllianceMissionViewModel.class);

            AllianceMission mission = missionVM.currentMission.getValue();

            if (mission != null && !mission.isFinished()) {
                missionVM.updateProgress(mission.getId(), action);
                Log.d("MissionProgress", "✅ Reported " + action + " for mission " + mission.getId());
            } else {
                Log.d("MissionProgress", "⚠️ No active mission or already finished.");
            }
        } catch (Exception e) {
            Log.e("MissionProgress", "Error reporting progress: " + e.getMessage());
        }
    }

    // 🔹 Helper metode za svaku akciju
    public static void reportShopPurchase(Context context) {
        reportProgress(context, MissionAction.SHOP_PURCHASE);
    }

    public static void reportBossHit(Context context) {
        reportProgress(context, MissionAction.BOSS_HIT);
    }

    public static void reportEasyTask(Context context) {
        reportProgress(context, MissionAction.EASY_TASK);
    }

    public static void reportHardTask(Context context) {
        reportProgress(context, MissionAction.HARD_TASK);
    }

    public static void reportNoFailedTasks(Context context) {
        reportProgress(context, MissionAction.NO_FAILED_TASKS);
    }

    public static void reportMessageSent(Context context) {
        reportProgress(context, MissionAction.MESSAGE_SENT);
    }
}
