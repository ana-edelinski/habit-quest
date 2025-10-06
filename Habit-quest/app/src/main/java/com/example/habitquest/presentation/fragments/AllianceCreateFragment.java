package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.adapters.SelectableFriendsAdapter;
import com.example.habitquest.presentation.viewmodels.AllianceCreateViewModel;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;
import com.example.habitquest.presentation.viewmodels.factories.AllianceCreateViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.MyFriendsViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AllianceCreateFragment extends Fragment {

    private EditText etAllianceName;
    private RecyclerView rvFriends;
    private Button btnCreate;
    private SelectableFriendsAdapter adapter;
    private MyFriendsViewModel myFriendsViewModel;
    private AllianceCreateViewModel allianceCreateViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etAllianceName = v.findViewById(R.id.etAllianceName);
        rvFriends = v.findViewById(R.id.rvFriendsList);
        btnCreate = v.findViewById(R.id.btnCreateAlliance);

        rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SelectableFriendsAdapter(new ArrayList<>());
        rvFriends.setAdapter(adapter);

        myFriendsViewModel = new ViewModelProvider(
                requireActivity(),
                new MyFriendsViewModelFactory(
                        new AppPreferences(requireContext()),
                        new UserRepository(requireContext())
                )
        ).get(MyFriendsViewModel.class);

        myFriendsViewModel.friends.observe(getViewLifecycleOwner(), friends -> {
            if (friends != null && !friends.isEmpty()) {
                adapter.setFriends(friends);
                rvFriends.setVisibility(View.VISIBLE);
            } else {
                adapter.setFriends(new ArrayList<>());
                rvFriends.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "No friends found.", Toast.LENGTH_SHORT).show();
            }
        });

        myFriendsViewModel.listenForFriendsRealtime();

        allianceCreateViewModel = new ViewModelProvider(
                this,
                new AllianceCreateViewModelFactory(new AllianceRepository(requireContext()))
        ).get(AllianceCreateViewModel.class);

        allianceCreateViewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        allianceCreateViewModel.creationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Navigation.findNavController(v).navigateUp();
            }
        });

        btnCreate.setOnClickListener(v1 -> handleCreateAlliance());
    }

    private void handleCreateAlliance() {
        String allianceName = etAllianceName.getText().toString().trim();
        List<User> selectedFriends = adapter.getSelectedFriends();

        if (allianceName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter alliance name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = currentUser.getUid();
        AppPreferences prefs = new AppPreferences(requireContext());
        String currentName = prefs.getUsername();

        List<String> requestUids = new ArrayList<>();
        for (User u : selectedFriends) {
            requestUids.add(u.getUid());
        }

        Alliance alliance = new Alliance(allianceName, currentUid, currentName, requestUids);

        allianceCreateViewModel.createAlliance(alliance);
    }
}
