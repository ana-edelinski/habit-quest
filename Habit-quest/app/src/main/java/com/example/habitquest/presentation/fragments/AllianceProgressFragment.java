package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.MemberMissionProgress;
import com.example.habitquest.presentation.adapters.AllianceProgressAdapter;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceMissionViewModelFactory;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AllianceProgressFragment extends Fragment {

    private TextView tvLeaderName, tvAllianceName;
    private RecyclerView rvMembers;
    private AllianceMissionViewModel missionViewModel;
    private AllianceProgressAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLeaderName = view.findViewById(R.id.tvLeaderName);
        tvAllianceName = view.findViewById(R.id.tvAllianceName);

        rvMembers = view.findViewById(R.id.rvAllianceMembersProgress);
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));

        AllianceMissionViewModelFactory factory = new AllianceMissionViewModelFactory(requireContext());
        missionViewModel = new ViewModelProvider(requireActivity(), factory).get(AllianceMissionViewModel.class);

        missionViewModel.currentAlliance.observe(getViewLifecycleOwner(), alliance -> {
            if (alliance != null)
                tvLeaderName.setText("Leader: " + alliance.getLeaderName());
                tvAllianceName.setText(alliance.getName());
        });

        missionViewModel.currentMission.observe(getViewLifecycleOwner(), mission -> {
            if (mission == null) return;

            Map<String, MemberMissionProgress> map = mission.getMemberProgress();
            List<MemberMissionProgress> members = new ArrayList<>(map.values());

            // Sortiraj od najvećeg doprinosa
            members.sort((a, b) -> Integer.compare(b.getHpReduced(), a.getHpReduced()));

            // mapiraj id -> ime (pošto su članovi već učitani u alliance objektu)
            Map<String, String> names = new HashMap<>();
            Alliance alliance = missionViewModel.currentAlliance.getValue();
            if (alliance != null && alliance.getMembers() != null) {
                for (String uid : alliance.getMembers()) {
                    missionViewModel.getUsername(uid, new RepositoryCallback<String>() {
                        @Override
                        public void onSuccess(String username) {
                            names.put(uid, username);

                            // kad dobijemo sve imena, ažuriraj adapter
                            if (names.size() == alliance.getMembers().size()) {
                                AllianceProgressAdapter adapter =
                                        new AllianceProgressAdapter(members, names, mission.getBossHP());
                                rvMembers.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            names.put(uid, "Unknown");
                        }
                    });
                }
            }

            adapter = new AllianceProgressAdapter(members, names, mission.getBossHP());
            rvMembers.setAdapter(adapter);
        });
    }
}
