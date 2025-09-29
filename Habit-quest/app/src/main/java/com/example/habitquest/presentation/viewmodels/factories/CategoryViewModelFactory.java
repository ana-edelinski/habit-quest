package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;

public class CategoryViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public CategoryViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CategoryViewModel.class)) {
            AppPreferences prefs = new AppPreferences(context);
            CategoryRepository repo = new CategoryRepository(context);
            return (T) new CategoryViewModel(prefs, repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
