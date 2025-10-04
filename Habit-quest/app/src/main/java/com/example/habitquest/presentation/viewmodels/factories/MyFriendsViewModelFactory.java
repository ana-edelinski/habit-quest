package com.example.habitquest.presentation.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.presentation.viewmodels.MyFriendsViewModel;

public class MyFriendsViewModelFactory implements ViewModelProvider.Factory {

    private final AppPreferences prefs;
    private final UserRepository repo;

    public MyFriendsViewModelFactory(AppPreferences prefs, UserRepository repo) {
        this.prefs = prefs;
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyFriendsViewModel.class)) {
            return (T) new MyFriendsViewModel(prefs, repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
