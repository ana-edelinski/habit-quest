package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.presentation.adapters.FriendRequestsAdapter;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;
import com.example.habitquest.presentation.viewmodels.factories.MyFriendsViewModelFactory;

public class FriendRequestsFragment extends Fragment {

    private MyFriendsViewModel myFriendsViewModel;
    private FriendRequestsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        myFriendsViewModel = new ViewModelProvider(requireActivity(),
                new MyFriendsViewModelFactory(new AppPreferences(requireContext()), new UserRepository(requireContext()))
        ).get(MyFriendsViewModel.class);
        adapter = new FriendRequestsAdapter();

        RecyclerView rv = v.findViewById(R.id.rvFriendRequests);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        adapter.setOnRequestActionListener(new FriendRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(String requesterUid) {
                adapter.removeByUid(requesterUid);
                myFriendsViewModel.acceptFriendRequest(requesterUid);
            }

            @Override
            public void onReject(String requesterUid) {
                adapter.removeByUid(requesterUid);
                myFriendsViewModel.rejectFriendRequest(requesterUid);
            }
        });

        TextView placeholder = v.findViewById(R.id.tvRequestsPlaceholder);

        myFriendsViewModel.friendRequestsUsers.observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                adapter.submitList(list);
                rv.setVisibility(View.VISIBLE);
                placeholder.setVisibility(View.GONE);
            } else {
                placeholder.setText("No friend requests.");
                rv.setVisibility(View.VISIBLE);
            }
        });

        myFriendsViewModel.listenForFriendRequestsRealtime();
    }
}
