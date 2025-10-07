package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.adapters.SelectableFriendsAdapter;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;
import com.example.habitquest.presentation.viewmodels.factories.MyFriendsViewModelFactory;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AllianceInviteFragment extends Fragment {

    private RecyclerView rvFriends;
    private Button btnSendInvites;
    private SelectableFriendsAdapter adapter;
    private MyFriendsViewModel myFriendsViewModel;
    private String allianceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rvFriends = v.findViewById(R.id.rvFriendsToInvite);
        btnSendInvites = v.findViewById(R.id.btnSendInvites);

        allianceId = getArguments() != null ? getArguments().getString("allianceId") : null;

        adapter = new SelectableFriendsAdapter(new ArrayList<>());
        rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFriends.setAdapter(adapter);

        myFriendsViewModel = new ViewModelProvider(
                requireActivity(),
                new MyFriendsViewModelFactory(
                        new AppPreferences(requireContext()),
                        new UserRepository(requireContext())
                )
        ).get(MyFriendsViewModel.class);

        if (allianceId != null) {
            FirebaseFirestore.getInstance()
                    .collection("alliances")
                    .document(allianceId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            List<String> members = (List<String>) snapshot.get("members");
                            adapter.setDisabled(members);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to load alliance members.", Toast.LENGTH_SHORT).show());
        }

        myFriendsViewModel.friends.observe(getViewLifecycleOwner(), friends -> {
            if (friends != null && !friends.isEmpty()) {
                adapter.setFriends(friends);
            } else {
                Toast.makeText(requireContext(), "No friends found.", Toast.LENGTH_SHORT).show();
            }
        });

        myFriendsViewModel.listenForFriendsRealtime();

        btnSendInvites.setOnClickListener(view -> sendInvites(view));
    }

    private void sendInvites(View view) {
        List<User> selectedFriends = adapter.getSelectedFriends();

        if (selectedFriends.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one friend to invite.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allianceId == null) {
            Toast.makeText(requireContext(), "Alliance not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AllianceRepository allianceRepo = new AllianceRepository(requireContext());

        final int total = selectedFriends.size();
        final int[] successCount = {0};
        final int[] failureCount = {0};

        for (User u : selectedFriends) {
            db.collection("users")
                    .document(u.getUid())
                    .update("allianceInvites", com.google.firebase.firestore.FieldValue.arrayUnion(allianceId))
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;
                        if (successCount[0] + failureCount[0] == total) {
                            onInvitesFinished(view, successCount[0], failureCount[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        failureCount[0]++;
                        if (successCount[0] + failureCount[0] == total) {
                            onInvitesFinished(view, successCount[0], failureCount[0]);
                        }
                    });
        }
    }

    private void onInvitesFinished(View view, int success, int fail) {
        if (fail == 0) {
            Toast.makeText(requireContext(),
                    "Successfully invited " + success + " friends!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                    "Invited " + success + " friends, " + fail + " failed.",
                    Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(view).navigateUp();
    }
}
