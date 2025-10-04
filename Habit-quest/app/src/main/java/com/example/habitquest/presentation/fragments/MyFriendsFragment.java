package com.example.habitquest.presentation.fragments;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.presentation.adapters.FriendsAdapter;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;
import com.example.habitquest.presentation.viewmodels.factories.MyFriendsViewModelFactory;

public class MyFriendsFragment extends Fragment {

    private MyFriendsViewModel myFriendsViewModel;
    private FriendsAdapter friendsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        myFriendsViewModel = new ViewModelProvider(requireActivity(),
                new MyFriendsViewModelFactory(new AppPreferences(requireContext()), new UserRepository(requireContext()))
        ).get(MyFriendsViewModel.class);

        TextView placeholder = v.findViewById(R.id.tvFriendsPlaceholder);
        RecyclerView rv = v.findViewById(R.id.rvFriends);
        Button btnRequests = v.findViewById(R.id.btnFriendRequests);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        friendsAdapter = new FriendsAdapter();
        rv.setAdapter(friendsAdapter);

        // ✅ Klik na prijatelja
        friendsAdapter.setOnFriendClickListener(friend -> {
            Bundle bundle = new Bundle();
            bundle.putString("uid", friend.getUid());
            Navigation.findNavController(v).navigate(R.id.nav_user_profile, bundle);
        });

        myFriendsViewModel.friends.observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                friendsAdapter.submitList(list);
                rv.setVisibility(View.VISIBLE);
                placeholder.setVisibility(View.GONE);
            } else {
                placeholder.setText("No friends yet.");
                rv.setVisibility(View.GONE);
                placeholder.setVisibility(View.VISIBLE);
            }
        });

        myFriendsViewModel.listenForFriendsRealtime();

        btnRequests.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(R.id.friendRequestsFragment)
        );
    }

}
