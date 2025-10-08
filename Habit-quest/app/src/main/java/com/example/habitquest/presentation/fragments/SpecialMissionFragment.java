package com.example.habitquest.presentation.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieAnimationView;
import com.example.habitquest.R;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MemberMissionProgress;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceMissionViewModelFactory;
import com.google.firebase.Timestamp;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SpecialMissionFragment extends Fragment {

    private AllianceMissionViewModel missionViewModel;
    private LinearLayout layoutNoAlliance, layoutNoMission;
    private ScrollView layoutMissionActive;
    private TextView tvNoMissionMessage, tvBossHp;
    private CountDownTimer missionTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_special_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutNoAlliance = view.findViewById(R.id.layoutNoAlliance);
        layoutNoMission = view.findViewById(R.id.layoutNoMission);
        layoutMissionActive = view.findViewById(R.id.layoutMissionActive);
        tvNoMissionMessage = view.findViewById(R.id.tvNoMissionMessage);
        Button btnJoinAlliance = view.findViewById(R.id.btnJoinAlliance);
        Button btnShowLastResult = view.findViewById(R.id.btnShowLastResult);

        AllianceMissionViewModelFactory factory = new AllianceMissionViewModelFactory(requireContext());
        missionViewModel = new ViewModelProvider(requireActivity(), factory).get(AllianceMissionViewModel.class);

        // ➜ navigacija ka prijateljima
        btnJoinAlliance.setOnClickListener(v1 ->
                Navigation.findNavController(v1).navigate(R.id.action_specialMissionFragment_to_myFriendsFragment)
        );

        // ➜ učitavanje saveza i misije
        missionViewModel.loadAllianceForUser();






        // ➜ fejk “instant kraj” za testiranje (long click na boss HP)
        tvBossHp = view.findViewById(R.id.tvBossHp);
        tvBossHp.setOnLongClickListener(v1 -> {
            AllianceMission mission = missionViewModel.currentMission.getValue();
            if (mission != null && mission.isActive()) {
                mission.finish(true);              // označi da je misija završena
                missionViewModel.finishMission(); // ažuriraj u Firestore
            }
            return true;
        });

        // ➜ posmatranje saveza
        missionViewModel.currentAlliance.observe(getViewLifecycleOwner(), alliance -> {
            layoutNoAlliance.setVisibility(View.GONE);
            layoutNoMission.setVisibility(View.GONE);
            layoutMissionActive.setVisibility(View.GONE);
            btnShowLastResult.setVisibility(View.GONE);

            if (alliance == null) {
                layoutNoAlliance.setVisibility(View.VISIBLE);
                return;
            }

            if (!alliance.isMissionActive()) {
                missionViewModel.loadLastFinishedMission(alliance.getId());

                layoutNoMission.setVisibility(View.VISIBLE);
                String message = "Contact your alliance " + alliance.getName() + " leader " + alliance.getLeaderName() +
                        " to start a mission.";
                tvNoMissionMessage.setText(message);
                return;
            }

            // 🔹 Ako je aktivna misija
            layoutMissionActive.setVisibility(View.VISIBLE);
        });

        // ➜ posmatranje poslednje misije
        missionViewModel.lastFinishedMission.observe(getViewLifecycleOwner(), mission -> {
            if (mission != null) {
                // 🔹 prikaži dugme i dodaj animaciju da se vidi
                btnShowLastResult.setVisibility(View.VISIBLE);
                btnShowLastResult.setAlpha(0f);
                btnShowLastResult.animate().alpha(1f).setDuration(300).start();

                btnShowLastResult.setOnClickListener(v -> {
                    showMissionEndDialog(mission.isVictory());
                });
            } else {
                btnShowLastResult.setVisibility(View.GONE);
            }
        });


        // ➜ posmatranje misije
        missionViewModel.currentMission.observe(getViewLifecycleOwner(), mission -> {
            if (mission == null) return;

            Alliance alliance = missionViewModel.currentAlliance.getValue();
            String currentUid = missionViewModel.getRemoteUid();

            // korisnik nije član saveza
            if (alliance == null || alliance.getMembers() == null ||
                    !alliance.getMembers().contains(currentUid)) {
                return;
            }

            // ➜ ako je HP 0 ili isteklo vreme — kraj misije
            if (mission.isFinished()) {
                missionViewModel.finishMission();
                return;
            }

            // ➜ prikaz trenutnog stanja
            layoutMissionActive.setVisibility(View.VISIBLE);

            ProgressBar progressBossHp = view.findViewById(R.id.progressBossHp);
            ProgressBar progressUserHp = view.findViewById(R.id.progressUserHp);
            TextView tvBossHp = view.findViewById(R.id.tvBossHp);
            TextView tvUserDamage = view.findViewById(R.id.tvUserDamage);
            TextView tvMissionTimer = view.findViewById(R.id.tvMissionTimer);

            long bossMaxHP = mission.getBossHP();
            long bossRemainingHP = mission.getRemainingHP();
            long bossLostHP = bossMaxHP - bossRemainingHP;
            double totalProgress = (bossLostHP * 100.0) / bossMaxHP;

            progressBossHp.setProgress((int) totalProgress);
            tvBossHp.setText("HP: " + bossRemainingHP + " / " + bossMaxHP);

            // ➜ napredak trenutnog korisnika
            String uid = missionViewModel.getRemoteUid();
            MemberMissionProgress progress = mission.getMemberProgress().get(uid);
            int memberDamage = (progress != null) ? progress.getHpReduced() : 0;
            double memberPercent = (memberDamage * 100.0) / bossMaxHP;

            progressUserHp.setProgress((int) memberPercent);
            tvUserDamage.setText("You dealt " + memberDamage + " HP damage");

            // ➜ timer do kraja
            Timestamp end = mission.getEndDate();
            if (end != null) {
                long millisLeft = end.toDate().getTime() - System.currentTimeMillis();
                if (millisLeft > 0) startMissionTimer(tvMissionTimer, millisLeft);
                else tvMissionTimer.setText("Mission ended");
            }
        });




        // ➜ dugme za napredak saveza
        Button btnAllianceProgress = view.findViewById(R.id.btnAllianceProgress);
        btnAllianceProgress.setOnClickListener(v1 ->
                Navigation.findNavController(v1).navigate(R.id.action_specialMissionFragment_to_allianceProgressFragment)
        );
    }

    // 🕒 stabilan timer
    private void startMissionTimer(TextView tvTimer, long durationMillis) {
        if (missionTimer != null) missionTimer.cancel();

        missionTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return;

                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                String timeFormatted = (days > 0)
                        ? String.format(Locale.getDefault(), "%dd %02dh %02dm %02ds", days, hours, minutes, seconds)
                        : String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                tvTimer.setText("⏳ " + timeFormatted);

                if (millisUntilFinished < 10 * 60 * 1000) {
                    tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                } else if (millisUntilFinished < 60 * 60 * 1000) {
                    tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                } else {
                    tvTimer.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onFinish() {
                if (!isAdded()) return;
                tvTimer.setText("💀 Mission ended");
                tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                tvTimer.setTypeface(null, Typeface.BOLD);
                missionViewModel.finishMission(); // ➜ automatski kraj
            }
        }.start();
    }

    private void showMissionEndDialog(boolean victory) {
        if (getActivity() == null) return;

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reward);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        LottieAnimationView lottieChest = dialog.findViewById(R.id.lottieChest);
        TextView tvTitle = dialog.findViewById(R.id.tvRewardTitle);
        TextView tvCoins = dialog.findViewById(R.id.tvCoinsReward);
        TextView tvEquipment = dialog.findViewById(R.id.tvEquipmentReward);
        Button btnClose = dialog.findViewById(R.id.btnCloseReward);

        if (victory) {
            // 🔹 Pobeda
            lottieChest.setAnimation(R.raw.chest_opening);
            tvTitle.setText("Mission Complete!");
            tvCoins.setText("× 250 coins");
            tvEquipment.setText("You received a potion and new clothing!");
        } else {
            // 🔹 Poraz
            lottieChest.setAnimation(R.raw.mission_failed);
            tvTitle.setText("Mission Failed");
            tvCoins.setText("No rewards this time...");
            tvEquipment.setText("Better luck next time!");
        }

        lottieChest.playAnimation();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (missionTimer != null) {
            missionTimer.cancel();
            missionTimer = null;
        }
    }
}
