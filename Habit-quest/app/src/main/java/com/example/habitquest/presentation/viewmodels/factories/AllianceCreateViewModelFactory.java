package com.example.habitquest.presentation.viewmodels.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.presentation.viewmodels.AllianceCreateViewModel;

public class AllianceCreateViewModelFactory implements ViewModelProvider.Factory {

    private final AllianceRepository repository;

    public AllianceCreateViewModelFactory(AllianceRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AllianceCreateViewModel.class)) {
            return (T) new AllianceCreateViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
