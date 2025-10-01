package com.example.habitquest.presentation.viewmodels.factories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;

public class TaskViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public TaskViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TaskViewModel.class)) {
            AppPreferences prefs = new AppPreferences(context);
            TaskRepository taskRepo = new TaskRepository(context);
            UserXpLogRepository xpRepo = new UserXpLogRepository(context);
            UserRepository userRepo = new UserRepository(context);
            TaskOccurrenceRepository occurrenceRepo = new TaskOccurrenceRepository(context);
            return (T) new TaskViewModel(prefs, taskRepo, xpRepo, userRepo, occurrenceRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
