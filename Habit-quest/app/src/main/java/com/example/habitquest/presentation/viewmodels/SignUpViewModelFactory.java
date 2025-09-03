package com.example.habitquest.presentation.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.UserRepository;

public class SignUpViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public SignUpViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SignUpViewModel.class)) {
            UserRepository repo = new UserRepository(context);
            return (T) new SignUpViewModel(repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

