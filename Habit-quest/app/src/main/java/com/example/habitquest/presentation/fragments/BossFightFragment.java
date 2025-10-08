package com.example.habitquest.presentation.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.habitquest.R;
import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.presentation.adapters.ActiveEquipmentAdapter;
import com.example.habitquest.presentation.viewmodels.BossFightViewModel;
import com.example.habitquest.presentation.viewmodels.factories.BossFightViewModelFactory;
import com.example.habitquest.utils.MissionProgressHelper;
import com.google.android.material.button.MaterialButton;

/**
 * Glavni fragment za borbu sa bosom.
 * Prikazuje trenutnog bossa, status borbe i omogućava napade.
 */
public class BossFightFragment extends Fragment {

    private BossFightViewModel viewModel;

    private ImageView imgBoss;
    private TextView tvBossTitle;
    private ProgressBar progressBossHp;
    private ProgressBar progressPlayerPp;
    private TextView tvSuccessRate;
    private TextView tvRemainingAttacks;
    private TextView tvBattleFeedback;
    private  TextView  tvBossHpValue;
    private  TextView tvPlayerPpValue;
    private MaterialButton btnAttack;

    private RecyclerView rvActiveEquipment;
    private ActiveEquipmentAdapter equipmentAdapter;

    private ImageButton btnRewards;
    private PopupWindow rewardsPopup;
    private MediaPlayer hitSound;


    private int lastBossHp = 0;


    // --- 🔹 Shake sensor polja ---
    private SensorManager sensorManager;
    private float accelCurrent;
    private float accelLast;
    private float shake;
    private static final float SHAKE_THRESHOLD = 12f; // prag za prepoznavanje treskanja
    private long lastShakeTime = 0;
    private static final int SHAKE_COOLDOWN_MS = 1000;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_boss_fight, container, false);
        initViews(v);
        setupViewModel();
        setupObservers();
        setupListeners();

        // 🔹 Učitaj bossa i pripremi podatke za borbu
        viewModel.loadCurrentBoss();
        viewModel.prepareBattleData();

        hitSound = MediaPlayer.create(requireContext(), R.raw.hit_sound);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);



        return v;
    }

    // ------------------------------------------------------------
    // 🔹 Inicijalizacija UI elemenata
    // ------------------------------------------------------------
    private void initViews(View v) {
        imgBoss = v.findViewById(R.id.imgBoss);
        tvBossTitle = v.findViewById(R.id.tvBossTitle);
        progressBossHp = v.findViewById(R.id.progressBossHp);
        progressPlayerPp = v.findViewById(R.id.progressPlayerPp);
        tvSuccessRate = v.findViewById(R.id.tvSuccessRate);
        tvRemainingAttacks = v.findViewById(R.id.tvRemainingAttacks);
        tvBattleFeedback = v.findViewById(R.id.tvBattleFeedback);
        btnAttack = v.findViewById(R.id.btnAttack);
        tvBossHpValue = v.findViewById(R.id.tvBossHpValue);
        tvPlayerPpValue = v.findViewById(R.id.tvPlayerPpValue);

        rvActiveEquipment = v.findViewById(R.id.rvActiveEquipment);
        rvActiveEquipment.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        equipmentAdapter = new ActiveEquipmentAdapter();
        rvActiveEquipment.setAdapter(equipmentAdapter);
        btnRewards = v.findViewById(R.id.btnRewards);



    }

    // ------------------------------------------------------------
    // 🔹 ViewModel i observeri
    // ------------------------------------------------------------
    private void setupViewModel() {
        BossFightViewModelFactory factory = new BossFightViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(BossFightViewModel.class);
    }

    private void setupObservers() {
        // 🟣 Kad se učita trenutni boss
        viewModel.currentBoss.observe(getViewLifecycleOwner(), boss -> {
            if (boss != null) {
                tvBossTitle.setText("Boss Level " + boss.getLevel());
                imgBoss.setImageResource(getBossImageRes(boss.getLevel()));
                if (viewModel.battleStats.getValue() != null) {
                    progressBossHp.setProgress(viewModel.battleStats.getValue().getBossHP());
                }


                progressBossHp.setMax(boss.getMaxHp());
                progressBossHp.setProgress(boss.getHp());
                lastBossHp = boss.getHp();
            }
        });

        viewModel.potentialRewards.observe(getViewLifecycleOwner(), rewards -> {
            if (rewards != null) {
                btnRewards.setOnClickListener(v ->
                        showRewardsPopup(v, rewards.getMinCoins(), rewards.getMaxCoins(), rewards.getEquipmentChance())
                );
            }
        });

        viewModel.activeEquipment.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                equipmentAdapter.submitList(items);
            }
        });


        // 🟢 Kad se promene statistike borbe
        viewModel.battleStats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) updateBattleUI(stats);
        });

        viewModel.battleResult.observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                btnAttack.setEnabled(false);

                if (result.isVictory()) {
                    showRewardDialog(result);
                } else {
                    Toast.makeText(requireContext(), "Defeat! Level up to fight again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // ------------------------------------------------------------
    // 🔹 Listener za Attack dugme
    // ------------------------------------------------------------
    private void setupListeners() {
        btnAttack.setOnClickListener(v -> {
            viewModel.performAttack();
        });

    }

    // ------------------------------------------------------------
    // 🔹 Pomoćne metode
    // ------------------------------------------------------------
    private void updateBattleUI(BattleStats stats) {
        // ažuriraj HP bara bossa
        progressBossHp.setProgress(stats.getBossHP());
        int usedAttacks = stats.getTotalAttempts() - stats.getRemainingAttempts();
        if (usedAttacks > 0) { // prikazuj feedback tek posle prvog napada
            int hpDiff = lastBossHp - stats.getBossHP();
            if (hpDiff > 0) {
                MissionProgressHelper.reportBossHit(requireActivity());
                hitSound.start();
                tvBattleFeedback.setText("Hit!");
                tvBattleFeedback.setTextColor(getResources().getColor(R.color.green));
                imgBoss.setImageResource(R.drawable.boss_take_hit);
                new Handler().postDelayed(() ->
                                imgBoss.setImageResource(R.drawable.boss_idle),
                        300
                );

            } else {
                tvBattleFeedback.setText("Miss!");
                tvBattleFeedback.setTextColor(getResources().getColor(R.color.red));
            }
        }

        tvBossHpValue.setText(stats.getBossHP() + " / " + progressBossHp.getMax());
        tvPlayerPpValue.setText((String.valueOf(stats.getUserPP())));


        lastBossHp = stats.getBossHP();

        // osveži preostale napade i šansu
        int used = 5 - stats.getRemainingAttempts();
        tvRemainingAttacks.setText("Attack " + used + "/"+ stats.getTotalAttempts());
        tvSuccessRate.setText("Chance: " + (int) stats.getSuccessRate() + "%");

        // PP bar — za sada statičan, ali kasnije možeš dinamički menjati
        progressPlayerPp.setProgress(stats.getUserPP());

        // ako je borba gotova, deaktiviraj dugme
        if (stats.isBattleOver()) {
            btnAttack.setEnabled(false);
            tvBattleFeedback.setText(stats.isVictory() ? "Victory!" : "Defeat!");
            tvBattleFeedback.setTextColor(
                    getResources().getColor(stats.isVictory() ? R.color.green : R.color.red)
            );



        }
    }


    private void showRewardsPopup(View anchor, int minCoins, int maxCoins, double equipmentChance) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_rewards, null);
        rewardsPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        TextView tvCoinRange = popupView.findViewById(R.id.tvCoinRange);
        TextView tvEquipmentChance = popupView.findViewById(R.id.tvEquipmentChance);

        // 🔹 Postavi vrednosti
        tvCoinRange.setText(minCoins + " – " + maxCoins + " coins");
        tvEquipmentChance.setText((int) (equipmentChance * 100)
                + "% chance for equipment (95% clothing / 5% weapon)");

        rewardsPopup.setElevation(10f);
        rewardsPopup.showAsDropDown(anchor, -200, 20);
    }





    private void showRewardDialog(BossFightResult result) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reward);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.6f); // polumračni sloj iza dijaloga
            window.setGravity(Gravity.CENTER);
        }

        LottieAnimationView lottieChest = dialog.findViewById(R.id.lottieChest);
        ImageView ivCoin = dialog.findViewById(R.id.ivCoinIcon);
        TextView tvCoins = dialog.findViewById(R.id.tvCoinsReward);
        TextView tvEquipment = dialog.findViewById(R.id.tvEquipmentReward);
        Button btnClose = dialog.findViewById(R.id.btnCloseReward);

        // 🪙 Prikaz osvojenih novčića
        tvCoins.setText("× " + result.getEarnedCoins());

        // 🛡️ Ako je osvojio opremu
        if (result.getEquipmentId() != null) {
            tvEquipment.setText("You found new " + result.getEquipmentId() + "!");
            tvEquipment.setVisibility(View.VISIBLE);
        } else {
            tvEquipment.setVisibility(View.GONE);
        }

        // 🔹 Animacija kovčežića
        lottieChest.playAnimation();

        // 🔹 Zatvori dijalog
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    /**
     * Povezuje nivo bossa sa odgovarajućom slikom iz drawable foldera.
     */
    private int getBossImageRes(int level) {
        switch (level) {
            case 1: return R.drawable.boss_idle;
            case 2: return R.drawable.boss_level_2;
            case 3: return R.drawable.boss_level_3;
            case 4: return R.drawable.boss_level_4;
            default: return R.drawable.boss_idle;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (hitSound != null) {
            hitSound.release();
            hitSound = null;
        }
    }


    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = accelCurrent - accelLast;
            shake = shake * 0.9f + delta;

            long now = System.currentTimeMillis();
            if (shake > SHAKE_THRESHOLD && (now - lastShakeTime > SHAKE_COOLDOWN_MS)) {
                lastShakeTime = now;
                viewModel.performAttack();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };


}
