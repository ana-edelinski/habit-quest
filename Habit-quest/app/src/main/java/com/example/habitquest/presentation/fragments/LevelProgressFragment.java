package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AccountViewModelFactory;
import com.example.habitquest.utils.LevelUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LevelProgressFragment extends Fragment {

    private TextView tvTitle, tvLevel, tvXP, tvPoints, tvCoins;
    private ImageView ivTitleIcon;
    private LinearProgressIndicator progressXP;
    private AccountViewModel viewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_progress, container, false);

        View includeView = view.findViewById(R.id.includeLevelCard);

        tvTitle = includeView.findViewById(R.id.tvTitle);
        tvLevel = includeView.findViewById(R.id.tvLevel);
        ivTitleIcon = includeView.findViewById(R.id.ivTitleIcon);

        tvXP = view.findViewById(R.id.tvXP);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvCoins = view.findViewById(R.id.tvCoins);
        ivTitleIcon = view.findViewById(R.id.ivTitleIcon);
        progressXP = view.findViewById(R.id.progressXP);

        AccountViewModelFactory factory = new AccountViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(AccountViewModel.class);

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvTitle.setText(user.getTitle());
                tvLevel.setText("Level " + user.getLevel());

                ivTitleIcon.setImageResource(
                        com.example.habitquest.utils.TitleIconUtils.getIconForLevel(user.getLevel())
                );

                // XP bar
                int currentXp = user.getTotalXp();
                int currentLevel = user.getLevel();
                int xpForCurrentLevel = LevelUtils.getXpThresholdForLevel(currentLevel);
                int xpForNextLevel = LevelUtils.getXpThresholdForLevel(currentLevel + 1);

                int progress = currentXp - xpForCurrentLevel;
                int max = xpForNextLevel - xpForCurrentLevel;

                progressXP.setMax(max);
                progressXP.setProgress(progress);
                tvXP.setText(user.getTotalXp() + " / " + xpForNextLevel + " XP");

                tvPoints.setText("Points: " + user.getPp());
                tvCoins.setText("Coins: " + ""); // user.getCoins()
            }
        });

        viewModel.loadUser();

        return view;
    }
}
