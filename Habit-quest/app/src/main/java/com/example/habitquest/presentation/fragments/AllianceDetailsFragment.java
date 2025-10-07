package com.example.habitquest.presentation.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        Button btnDisband = v.findViewById(R.id.btnDisbandAlliance);

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

                if (user == null) return;

                boolean isLeader = alliance.getLeaderId().equals(user.getUid());

                btnInvite.setVisibility(isLeader ? View.VISIBLE : View.GONE);
                btnDisband.setVisibility(isLeader ? View.VISIBLE : View.GONE);
                btnLeave.setVisibility(!isLeader ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.members.observe(getViewLifecycleOwner(), adapter::setMembers);

        btnLeave.setOnClickListener(v1 -> {
            if (allianceId == null || user == null) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Leave Alliance")
                    .setMessage("Are you sure you want to leave this alliance?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.leaveAlliance(allianceId, user.getUid(), result -> {
                            if (result) {
                                Toast.makeText(requireContext(), "You left the alliance.", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(v1).navigateUp();
                            } else {
                                Toast.makeText(requireContext(), "Cannot leave while mission is active.", Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 🔹 Disband alliance (leader only)
        btnDisband.setOnClickListener(v12 -> {
            if (allianceId == null) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Disband Alliance")
                    .setMessage("Are you sure you want to disband this alliance? All members will be removed.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.disbandAlliance(allianceId, result -> {
                            if (result) {
                                Toast.makeText(requireContext(), "Alliance disbanded.", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(v12).navigateUp();
                            } else {
                                Toast.makeText(requireContext(), "Cannot disband while mission is active.", Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnInvite.setOnClickListener(view -> {
            if (viewModel.alliance.getValue() == null) return;

            Bundle bundle = new Bundle();
            bundle.putString("allianceId", viewModel.alliance.getValue().getId());
            Navigation.findNavController(view)
                    .navigate(R.id.allianceInviteFragment, bundle);
        });
    }
}
