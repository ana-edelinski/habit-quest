package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceMissionRepository;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.repositoryinterfaces.IAllianceMissionRepository;
import com.example.habitquest.presentation.viewmodels.AllianceMissionViewModel;

public class AllianceMissionViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public AllianceMissionViewModelFactory(Context context) {
        this.context = context;

    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AllianceMissionViewModel.class)) {
            AllianceMissionRepository repo = new AllianceMissionRepository();
            AllianceRepository allianceRepo = new AllianceRepository();
            UserRepository userRepository = new UserRepository(context);
            return (T) new AllianceMissionViewModel(new AppPreferences(context),repo,userRepository, allianceRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
