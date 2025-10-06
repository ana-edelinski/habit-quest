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
import androidx.navigation.fragment.NavHostFragment;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AccountViewModelFactory;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

public class HomeFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private ImageView ivAvatar, ivTitleIcon;
    private TextView tvHello, tvMotivation, tvTitle, tvLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🔹 Initialize UI elements
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvHello = view.findViewById(R.id.tvHello);
        tvMotivation = view.findViewById(R.id.tvMotivation);
        ivTitleIcon = view.findViewById(R.id.ivTitleIcon);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvLevel = view.findViewById(R.id.tvLevel);

        MaterialCardView levelCard = view.findViewById(R.id.levelCard);
        MaterialCardView cardStatistics = view.findViewById(R.id.cardStatistics);

        // 🔹 ViewModel setup
        AccountViewModelFactory factory = new AccountViewModelFactory(requireContext());
        accountViewModel = new ViewModelProvider(requireActivity(), factory).get(AccountViewModel.class);

        // 🔹 Observe user data
        accountViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvHello.setText("Hello, " + user.getUsername() + "!");

                String[] messages = {
                        "Small wins daily lead to massive change!",
                        "Stay loyal to your goals!",
                        "Your habits build your future!",
                        "1% better every day!"
                };
                tvMotivation.setText(messages[new Random().nextInt(messages.length)]);

                int resId;
                switch (user.getAvatar()) {
                    case 1: resId = R.drawable.avatar1; break;
                    case 2: resId = R.drawable.avatar2; break;
                    case 3: resId = R.drawable.avatar3; break;
                    case 4: resId = R.drawable.avatar4; break;
                    case 5: resId = R.drawable.avatar5; break;
                    default: resId = R.drawable.avatar5;
                }
                ivAvatar.setImageResource(resId);

                tvTitle.setText(user.getTitle());
                tvLevel.setText("Level " + user.getLevel());
                ivTitleIcon.setImageResource(
                        com.example.habitquest.utils.TitleIconUtils.getIconForLevel(user.getLevel())
                );
            }
        });

        accountViewModel.loadUser();

        // 🔹 Open Level Progress on click
        levelCard.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_nav_home_to_levelProgressFragment);
        });

        // 🔹 Open Statistics on click with animation
        cardStatistics.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.statisticsFragment);
            }).start();
        });

        return view;
    }
}
