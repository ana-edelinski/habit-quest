package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.presentation.viewmodels.ChangePasswordViewModel;

public class ChangePasswordViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ChangePasswordViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel.class)) {
            UserRepository userRepository = new UserRepository(context);
            AppPreferences appPreferences = new AppPreferences(context);
            return (T) new ChangePasswordViewModel(userRepository, appPreferences);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

