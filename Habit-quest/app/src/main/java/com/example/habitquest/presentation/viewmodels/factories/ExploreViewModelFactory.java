package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.presentation.viewmodels.ExploreViewModel;

public class ExploreViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public ExploreViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExploreViewModel.class)) {
            return (T) new ExploreViewModel(new UserRepository(context));
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
