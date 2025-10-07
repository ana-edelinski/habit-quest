package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceMissionRepository;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceMissionViewModelFactory;

public class SpecialMissionFragment extends Fragment {

    private AllianceMissionViewModel missionViewModel;

    private LinearLayout layoutNoAlliance, layoutNoMission, layoutMissionActive;
    private TextView tvNoMissionMessage;

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




        AllianceMissionViewModelFactory factory = new AllianceMissionViewModelFactory(requireContext());
        missionViewModel = new ViewModelProvider(this, factory).get(AllianceMissionViewModel.class);

        btnJoinAlliance.setOnClickListener(v1 ->
                Navigation.findNavController(v1).navigate(R.id.action_specialMissionFragment_to_myFriendsFragment)
        );

        // Učitaj savez i stanje misije
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
    }
}
