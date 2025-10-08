package com.example.habitquest.presentation.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceMissionRepository;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MemberMissionProgress;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceMissionViewModelFactory;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.Timestamp;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SpecialMissionFragment extends Fragment {

    private AllianceMissionViewModel missionViewModel;
    private LinearLayout layoutNoAlliance, layoutNoMission;
    private ScrollView layoutMissionActive;
    private TextView tvNoMissionMessage;
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

        // 🔹 koristimo shared ViewModel između fragmenata
        AllianceMissionViewModelFactory factory = new AllianceMissionViewModelFactory(requireContext());
        missionViewModel = new ViewModelProvider(requireActivity(), factory).get(AllianceMissionViewModel.class);

        btnJoinAlliance.setOnClickListener(v1 ->
                Navigation.findNavController(v1).navigate(R.id.action_specialMissionFragment_to_myFriendsFragment)
        );

        // 🔹 Učitaj savez i stanje misije
        missionViewModel.loadAllianceForUser();

        missionViewModel.currentAlliance.observe(getViewLifecycleOwner(), alliance -> {
            layoutNoAlliance.setVisibility(View.GONE);
            layoutNoMission.setVisibility(View.GONE);
            layoutMissionActive.setVisibility(View.GONE);

            if (alliance == null) {
                layoutNoAlliance.setVisibility(View.VISIBLE);
            } else if (!alliance.isMissionActive()) {
                layoutNoMission.setVisibility(View.VISIBLE);
                String message = "Contact your alliance " + alliance.getName() + " leader " + alliance.getLeaderName() +
                        " to start a mission.";
                tvNoMissionMessage.setText(message);
            } else {
                layoutMissionActive.setVisibility(View.VISIBLE);
            }
        });

        // 🔹 Posmatraj aktivnu misiju
        missionViewModel.currentMission.observe(getViewLifecycleOwner(), mission -> {
            if (mission == null) return;

            if (mission.isFinished()) {
                showMissionEndDialog(mission.getRemainingHP() <= 0);
                return;
            }

            long bossRemainingHP = mission.getRemainingHP();
//            if (bossRemainingHP <= 0 && !mission.isFinished()) {
//                missionViewModel.finishMission();
//                showMissionEndDialog(true);
//                return;
//            }

            layoutMissionActive.setVisibility(View.VISIBLE);

            ProgressBar progressBossHp = view.findViewById(R.id.progressBossHp);
            ProgressBar progressUserHp = view.findViewById(R.id.progressUserHp);
            TextView tvBossHp = view.findViewById(R.id.tvBossHp);
            TextView tvUserDamage = view.findViewById(R.id.tvUserDamage);
            TextView tvMissionTimer = view.findViewById(R.id.tvMissionTimer);


            tvMissionTimer.setShadowLayer(4f, 0f, 0f, Color.BLACK); // 💡 bela slova sa senkom

            long bossMaxHP = mission.getBossHP();
            bossRemainingHP = mission.getRemainingHP();
            long bossLostHP = bossMaxHP - bossRemainingHP;
            double totalProgress = (bossLostHP * 100.0) / bossMaxHP;

            progressBossHp.setProgress((int) totalProgress);
            tvBossHp.setText("HP: " + bossRemainingHP + " / " + bossMaxHP);

            // 🔸 Napredak trenutnog korisnika
            String uid = missionViewModel.getRemoteUid(); // getter u VM
            MemberMissionProgress progress = mission.getMemberProgress().get(uid);
            int memberDamage = (progress != null) ? progress.getHpReduced() : 0;
            double memberPercent = (memberDamage * 100.0) / bossMaxHP;

            progressUserHp.setProgress((int) memberPercent);
            tvUserDamage.setText("You dealt " + memberDamage + " HP damage");

            // 🔸 Odbrojavanje vremena do kraja
            Timestamp end = mission.getEndDate();
            if (end != null) {
                long millisLeft = end.toDate().getTime() - System.currentTimeMillis();
                if (millisLeft > 0) startMissionTimer(tvMissionTimer, millisLeft);
                else tvMissionTimer.setText("Mission ended");
            }


        });

        // 🔹 Dugme za napredak saveza
        Button btnAllianceProgress = view.findViewById(R.id.btnAllianceProgress);
        btnAllianceProgress.setOnClickListener(v1 ->
                Navigation.findNavController(v1).navigate(R.id.action_specialMissionFragment_to_allianceProgressFragment)
        );
    }

    // 🕒 stabilan timer koji se automatski prekida
    private void startMissionTimer(TextView tvTimer, long durationMillis) {
        // Ako već postoji aktivan timer, prekini ga
        if (missionTimer != null) missionTimer.cancel();

        missionTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return; // ⚡ fragment možda nije više aktivan

                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                String timeFormatted = (days > 0)
                        ? String.format(Locale.getDefault(), "%dd %02dh %02dm %02ds", days, hours, minutes, seconds)
                        : String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                tvTimer.setText("⏳ " + timeFormatted);

                // 🔥 boja po vremenu
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
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 🧹 obavezno očisti timer da spreči crash
        if (missionTimer != null) {
            missionTimer.cancel();
            missionTimer = null;
        }
    }




    private void showMissionEndDialog(boolean victory) {
        if (getActivity() == null) return;

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reward);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        LottieAnimationView lottieChest = dialog.findViewById(R.id.lottieChest);
        TextView tvTitle = dialog.findViewById(R.id.tvRewardTitle);
        TextView tvCoins = dialog.findViewById(R.id.tvCoinsReward);
        TextView tvEquipment = dialog.findViewById(R.id.tvEquipmentReward);
        Button btnClose = dialog.findViewById(R.id.btnCloseReward);

        if (victory) {
            lottieChest.setAnimation(R.raw.chest_opening);
            tvTitle.setText("Mission Complete!");
            tvCoins.setText("× 250");
            tvEquipment.setText("You found rare loot!");
        } else {
            lottieChest.setAnimation(R.raw.mission_failed);
            tvTitle.setText("Mission Failed");
            tvCoins.setText("No rewards this time...");
            tvEquipment.setText("Better luck next time!");
        }

        lottieChest.playAnimation();

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            // možeš navigirati nazad ili ostaviti prazno
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_specialMissionFragment_to_allianceProgressFragment);
        });

        dialog.show();
    }

}

