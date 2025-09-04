package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.viewmodels.LoginViewModel;

public class LoginViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public LoginViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(new UserRepository(context), new AppPreferences(context));
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
