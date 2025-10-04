package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.habitquest.R;
import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.presentation.viewmodels.BossFightViewModel;
import com.example.habitquest.presentation.viewmodels.factories.BossFightViewModelFactory;
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

    private int lastBossHp = 0;

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

                progressBossHp.setMax(boss.getMaxHp());
                progressBossHp.setProgress(boss.getHp());
                lastBossHp = boss.getHp();
                viewModel.prepareBattleData();
            }
        });

        // 🟢 Kad se promene statistike borbe
        viewModel.battleStats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) updateBattleUI(stats);
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
        int usedAttacks = 5 - stats.getRemainingAttempts();
        if (usedAttacks > 0) { // prikazuj feedback tek posle prvog napada
            int hpDiff = lastBossHp - stats.getBossHP();
            if (hpDiff > 0) {
                tvBattleFeedback.setText("Hit!");
                tvBattleFeedback.setTextColor(getResources().getColor(R.color.green));
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
        tvRemainingAttacks.setText("Attack " + used + "/5");
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

    /**
     * Povezuje nivo bossa sa odgovarajućom slikom iz drawable foldera.
     */
    private int getBossImageRes(int level) {
        switch (level) {
            case 1: return R.drawable.boss_level_1;
            case 2: return R.drawable.boss_level_2;
            case 3: return R.drawable.boss_level_3;
            case 4: return R.drawable.boss_level_4;
            default: return R.drawable.boss_level_1;
        }
    }
}
