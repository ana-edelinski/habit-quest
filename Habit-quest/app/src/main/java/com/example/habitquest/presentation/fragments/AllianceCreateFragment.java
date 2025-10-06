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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.adapters.SelectableFriendsAdapter;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;
import com.example.habitquest.presentation.viewmodels.factories.MyFriendsViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class AllianceCreateFragment extends Fragment {

    private EditText etAllianceName;
    private RecyclerView rvFriends;
    private Button btnCreate;
    private SelectableFriendsAdapter adapter;
    private MyFriendsViewModel myFriendsViewModel;

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

        // 🔹 RecyclerView setup
        rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SelectableFriendsAdapter(new ArrayList<>());
        rvFriends.setAdapter(adapter);

        // 🔹 ViewModel setup (isti kao u MyFriendsFragment)
        myFriendsViewModel = new ViewModelProvider(
                requireActivity(),
                new MyFriendsViewModelFactory(
                        new AppPreferences(requireContext()),
                        new UserRepository(requireContext())
                )
        ).get(MyFriendsViewModel.class);

        // 🔹 Posmatraj prijatelje i prikazuj ih
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

        // 🔹 Dugme za kreiranje saveza (samo validacija za sada)
        btnCreate.setOnClickListener(v1 -> {
            String allianceName = etAllianceName.getText().toString().trim();
            List<User> selectedFriends = adapter.getSelectedFriends();

            if (allianceName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter alliance name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedFriends.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one friend", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: implementacija čuvanja saveza i slanja poziva
            Toast.makeText(requireContext(),
                    "Alliance \"" + allianceName + "\" created with " + selectedFriends.size() + " members (pending logic)",
                    Toast.LENGTH_LONG).show();
        });
    }
}
