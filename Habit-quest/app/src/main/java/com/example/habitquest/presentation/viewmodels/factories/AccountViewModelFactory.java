package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;

public class AccountViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public AccountViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AccountViewModel.class)) {
            UserXpLogRepository repo = new UserXpLogRepository(context);
            return (T) new AccountViewModel(new AppPreferences(context), repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
