package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.presentation.adapters.AllianceMemberAdapter;
import com.example.habitquest.presentation.viewmodels.AllianceDetailsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AllianceDetailsFragment extends Fragment {

    private AllianceDetailsViewModel viewModel;
    private AllianceMemberAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView tvName = v.findViewById(R.id.tvAllianceName);
        TextView tvLeader = v.findViewById(R.id.tvAllianceLeader);
        RecyclerView rvMembers = v.findViewById(R.id.rvAllianceMembers);
        Button btnLeave = v.findViewById(R.id.btnLeaveAlliance);
        Button btnInvite = v.findViewById(R.id.btnInviteMembers);

        adapter = new AllianceMemberAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AllianceDetailsViewModel.class);

        String allianceId = getArguments() != null ? getArguments().getString("allianceId") : null;
        if (allianceId != null) viewModel.loadAlliance(allianceId);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        viewModel.alliance.observe(getViewLifecycleOwner(), alliance -> {
            if (alliance != null) {
                tvName.setText(alliance.getName());
                tvLeader.setText("Leader: " + alliance.getLeaderName());

                if (user != null && !alliance.getLeaderId().equals(user.getUid())) {
                    btnLeave.setVisibility(View.VISIBLE);
                } else {
                    btnLeave.setVisibility(View.GONE);
                }

                if (user != null && alliance.getLeaderId().equals(user.getUid())) {
                    btnInvite.setVisibility(View.VISIBLE);
                } else {
                    btnInvite.setVisibility(View.GONE);
                }

            }
        });

        viewModel.members.observe(getViewLifecycleOwner(), adapter::setMembers);

        btnLeave.setOnClickListener(v1 -> {
            // Ovdje kasnije dodajemo logiku leaveAlliance()
        });

        btnInvite.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("allianceId", viewModel.alliance.getValue().getId());
            Navigation.findNavController(view)
                    .navigate(R.id.allianceInviteFragment, bundle);
        });


    }
}
