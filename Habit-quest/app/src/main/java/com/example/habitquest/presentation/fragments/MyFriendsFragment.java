package com.example.habitquest.presentation.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;

public class MyFriendsFragment extends Fragment {

    private MyFriendsViewModel mViewModel;

    public static MyFriendsFragment newInstance() {
        return new MyFriendsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_friends, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MyFriendsViewModel.class);
        // TODO: Use the ViewModel
    }

}