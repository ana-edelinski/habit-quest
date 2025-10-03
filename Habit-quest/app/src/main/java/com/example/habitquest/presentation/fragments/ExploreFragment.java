package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.presentation.adapters.UserSearchAdapter;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.ExploreViewModel;
import com.example.habitquest.presentation.viewmodels.factories.ExploreViewModelFactory;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {

    private EditText etSearchUser;
    private RecyclerView rvUsers;
    private UserSearchAdapter adapter;
    private ExploreViewModel exploreViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearchUser = view.findViewById(R.id.etSearchUser);
        rvUsers = view.findViewById(R.id.rvUsers);

        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserSearchAdapter(user -> {
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            exploreViewModel.sendFriendRequest(currentUid, user.getUid(), new com.example.habitquest.utils.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(requireContext(), "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        rvUsers.setAdapter(adapter);

        ExploreViewModelFactory factory = new ExploreViewModelFactory(requireContext());
        exploreViewModel = new ViewModelProvider(this, factory).get(ExploreViewModel.class);

        // Observe results
        exploreViewModel.userSearchResults.observe(getViewLifecycleOwner(), adapter::setUsers);

        // Handle search action
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    exploreViewModel.searchUsers(query);
                } else {
                    adapter.setUsers(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }
}
