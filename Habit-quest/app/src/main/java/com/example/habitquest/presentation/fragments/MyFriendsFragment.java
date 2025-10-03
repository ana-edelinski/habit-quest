package com.example.habitquest.presentation.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;

public class MyFriendsFragment extends Fragment {

    private AccountViewModel accountViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dobijemo AccountViewModel sa istim scope-om (activity)
        accountViewModel = new ViewModelProvider(requireActivity())
                .get(AccountViewModel.class);

        TextView textView = view.findViewById(R.id.tvFriendsPlaceholder);

        accountViewModel.friends.observe(getViewLifecycleOwner(), friends -> {
            if (friends != null && !friends.isEmpty()) {
                textView.setText("Friends: " + friends.toString());
            } else {
                textView.setText("No friends yet.");
            }
        });

        // Pokreni učitavanje
        accountViewModel.loadFriends();
    }
}
