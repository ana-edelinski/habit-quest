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

import com.example.habitquest.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LevelProgressFragment extends Fragment {

    private TextView tvTitle, tvLevel, tvXP, tvPoints, tvCoins;
    private ImageView ivTitleIcon;
    private LinearProgressIndicator progressXP;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_progress, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvXP = view.findViewById(R.id.tvXP);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvCoins = view.findViewById(R.id.tvCoins);
        ivTitleIcon = view.findViewById(R.id.ivTitleIcon);
        progressXP = view.findViewById(R.id.progressXP);

        // TODO: kasnije setovati stvarne vrednosti iz baze
        tvTitle.setText("Novice Explorer");
        tvLevel.setText("Level 5");
        tvXP.setText("1200 / 2000 XP");
        tvPoints.setText("Points: 300");
        tvCoins.setText("Coins: 50");
        progressXP.setMax(2000);
        progressXP.setProgress(1200);

        return view;
    }
}
